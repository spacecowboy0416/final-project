package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.infra.s3.S3Properties;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 상품 대표 이미지를 외부 이미지 URL에서 내려받아 우리 스토리지로 업로드하는 컴포넌트다.
 *
 * 이 클래스가 다루는 URL/URI:
 * - 입력 URL: `searchedProduct.productImageUrl()`에 들어있는 원본 쇼핑 이미지 URL
 * - 출력 URL: `UploadedImage.storageUrl()`에 들어있는 우리 S3/CDN 접근 URL
 * - 출력 URI 성격의 경로: `UploadedImage.storagePath()`에 들어있는 S3 object key
 *
 * 즉 "어떤 이미지를 저장할지"가 아니라
 * "이미지 URL을 우리 스토리지 URL/경로로 바꾸는 과정"을 맡는다.
 */
@Component
@RequiredArgsConstructor
public class ProductImageUploader {
    private static final String PRODUCT_IMAGE_STORAGE_PATH = "products/naver";

    private final ProductImageDownloader productImageDownloader;
    private final ImageUploadPort imageUploadPort;
    private final S3Properties s3Properties;

    /**
     * 기존 상품이 아직 우리 스토리지 URL을 쓰지 않는 경우에만 이미지를 다운로드하고 S3에 업로드한다.
     *
     * 반환값이 null이면 기존 `product.image_url` 또는 원본 외부 URL을 그대로 사용하면 된다.
     */
    public UploadedProductImage uploadIfNeeded(ProductDto existingProduct, SearchedProduct searchedProduct) {
        if (!StringUtils.hasText(searchedProduct.productImageUrl())) {
            return null;
        }
        if (existingProduct != null && isManagedStorageUrl(existingProduct.getImageUrl())) {
            return null;
        }

        ProductImageDownloader.DownloadedProductImage downloadedProductImage =
            productImageDownloader.download(searchedProduct.productImageUrl());

        ImageUploadPort.UploadedImage uploadedImage = imageUploadPort.uploadImage(
            new ImageUploadPort.ImageUploadRequest(
                downloadedProductImage.imageBytes(),
                downloadedProductImage.mimeType(),
                PRODUCT_IMAGE_STORAGE_PATH
            )
        );
        return new UploadedProductImage(uploadedImage, downloadedProductImage.checksumSha256());
    }

    /**
     * product 테이블 `image_url` 컬럼에 저장할 최종 이미지 URL을 결정한다.
     *
     * 우선순위:
     * 1. 이미 DB에 저장된 `product.image_url`
     * 2. 방금 업로드한 S3/CDN `storageUrl`
     * 3. 원본 쇼핑 이미지 URL
     */
    public String resolveImageUrl(
        ProductDto existingProduct,
        SearchedProduct searchedProduct,
        UploadedProductImage uploadedProductImage
    ) {
        if (existingProduct != null && StringUtils.hasText(existingProduct.getImageUrl())) {
            return existingProduct.getImageUrl();
        }
        if (uploadedProductImage != null) {
            return uploadedProductImage.uploadedImage().storageUrl();
        }
        return searchedProduct.productImageUrl();
    }

    private boolean isManagedStorageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return false;
        }

        String normalizedBaseUrl = s3Properties.normalizedBaseUrl();
        if (StringUtils.hasText(normalizedBaseUrl) && imageUrl.startsWith(normalizedBaseUrl + "/")) {
            return true;
        }

        String s3Prefix = "https://%s.s3.%s.amazonaws.com/".formatted(
            s3Properties.getBucket(),
            s3Properties.getRegion()
        );
        return imageUrl.startsWith(s3Prefix);
    }

    /**
     * S3 업로드 결과를 product_image_metadata 테이블 입력에 넘기기 위한 불변 묶음이다.
     *
     * - `uploadedImage.storageUrl()`: 외부에서 접근할 이미지 URL
     * - `uploadedImage.storagePath()`: S3 object key 경로
     * - `checksumSha256`: 메타데이터 무결성 확인용 해시
     */
    public record UploadedProductImage(
        ImageUploadPort.UploadedImage uploadedImage,
        String checksumSha256
    ) {
    }
}
