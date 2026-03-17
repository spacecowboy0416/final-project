package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductSourceType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductImageMetadataDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import com.finalproject.coordi.recommendation.service.product.ProductImageUploader.UploadedProductImage;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 검색된 상품 데이터를 recommendation DB 테이블에 업로드하는 컴포넌트다.
 *
 * 이 클래스가 다루는 DB 테이블:
 * - `product`: 상품 기본 정보와 대표 이미지 URL을 upsert
 * - `product_image_metadata`: S3 업로드 결과의 mime type, size, storage URL/path, checksum을 insert
 *
 * 이 클래스는 DB 저장 순서를 책임지고,
 * 실제 이미지 업로드 자체는 `ProductImageUploader`에 위임한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataUploader {
    private final RecommendationMapper recommendationMapper;
    private final ProductMapper productMapper;
    private final ProductImageUploader productImageUploader;
    @Qualifier("recommendationPipelineExecutor")
    private final Executor pipelineExecutor;

    /**
     * [1] slot 단위로 병렬 실행한다.
     * [2] 각 slot 내부에서는 `product`, `product_image_metadata` 반영을 순차 처리한다.
     */
    public void uploadAll(
        NormalizedBlueprintDto validatedBlueprint,
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
        if (validatedBlueprint == null
            || validatedBlueprint.itemsBySlot() == null
            || searchedProductsBySlot == null
            || searchedProductsBySlot.isEmpty()) {
            return;
        }

        Map<CategoryType, CompletableFuture<Void>> futures = new EnumMap<>(CategoryType.class);
        searchedProductsBySlot.forEach((slotKey, searchedProducts) -> futures.put(
            slotKey,
            CompletableFuture.runAsync(
                () -> uploadSlotSafely(validatedBlueprint, slotKey, searchedProducts),
                pipelineExecutor
            )
        ));

        CompletableFuture.allOf(futures.values().toArray(CompletableFuture[]::new)).join();
    }

    private void uploadSlotSafely(
        NormalizedBlueprintDto validatedBlueprint,
        CategoryType slotKey,
        List<SearchedProduct> searchedProducts
    ) {
        if (slotKey == null || searchedProducts == null || searchedProducts.isEmpty()) {
            return;
        }

        RawBlueprintDto.ItemInfo validatedItem = resolveValidatedItem(validatedBlueprint, slotKey);
        Long categoryId = resolveCategoryId(slotKey);
        Map<String, SearchedProduct> uniqueProducts = deduplicate(searchedProducts);

        for (SearchedProduct product : uniqueProducts.values()) {
            try {
                uploadProductRow(validatedBlueprint, validatedItem, product, categoryId);
            } catch (Exception exception) {
                log.warn(
                    "slot searched product upload skipped slot={} source={} externalId={} reason={}",
                    slotKey.code(),
                    product.marketplaceProvider(),
                    product.marketplaceProductId(),
                    exception.getMessage(),
                    exception
                );
            }
        }
    }

    /**
     * 검색된 상품 1건을 `product` upsert 후 필요 시 `product_image_metadata`에 이미지 메타데이터를 insert 한다.
     */
    private void uploadProductRow(
        NormalizedBlueprintDto validatedBlueprint,
        RawBlueprintDto.ItemInfo validatedItem,
        SearchedProduct product,
        Long categoryId
    ) {
        if (!StringUtils.hasText(product.marketplaceProductId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ProductDto existing = recommendationMapper.findProductBySourceAndExternalId(
            ProductSourceType.NAVER.code(),
            product.marketplaceProductId()
        );

        UploadedProductImage uploadedImage = productImageUploader.uploadIfNeeded(existing, product);
        String imageUrl = productImageUploader.resolveImageUrl(existing, product, uploadedImage);

        ProductDto row = productMapper.toProductDto(
            null,
            validatedBlueprint,
            validatedItem,
            product,
            categoryId,
            imageUrl
        );

        recommendationMapper.upsertProduct(row);
        Long productId = recommendationMapper.findProductIdBySourceAndExternalId(
            ProductSourceType.NAVER.code(),
            product.marketplaceProductId()
        );
        if (productId == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }

        if (uploadedImage != null) {
            recommendationMapper.insertProductImageMetadata(
                ProductImageMetadataDto.builder()
                    .productId(productId)
                    .mimeType(uploadedImage.uploadedImage().mimeType())
                    .imageSizeBytes(uploadedImage.uploadedImage().imageSizeBytes())
                    .storageProvider(uploadedImage.uploadedImage().storageProvider())
                    .storageUrl(uploadedImage.uploadedImage().storageUrl())
                    .storagePath(uploadedImage.uploadedImage().storagePath())
                    .checksumSha256(uploadedImage.checksumSha256())
                    .build()
            );
        }
    }

    private Map<String, SearchedProduct> deduplicate(List<SearchedProduct> searchedProducts) {
        Map<String, SearchedProduct> deduplicated = new LinkedHashMap<>();
        for (SearchedProduct searchedProduct : searchedProducts) {
            if (searchedProduct == null || !StringUtils.hasText(searchedProduct.marketplaceProductId())) {
                continue;
            }
            deduplicated.putIfAbsent(searchedProduct.marketplaceProductId(), searchedProduct);
        }
        return deduplicated;
    }

    private RawBlueprintDto.ItemInfo resolveValidatedItem(
        NormalizedBlueprintDto validatedBlueprint,
        CategoryType slotKey
    ) {
        if (validatedBlueprint == null || validatedBlueprint.itemsBySlot() == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }

        RawBlueprintDto.ItemInfo validatedItem = validatedBlueprint.itemBySlot(slotKey);
        if (validatedItem == null) {
            throw new BusinessException(
                "validated blueprint slot missing: %s".formatted(slotKey.code()),
                ErrorCode.DATABASE_ERROR
            );
        }
        return validatedItem;
    }

    private Long resolveCategoryId(CategoryType slotKey) {
        Long categoryId = recommendationMapper.findCategoryIdByCode(productMapper.resolveCategoryCode(slotKey));
        if (categoryId == null) {
            throw new BusinessException(
                "category code not found: %s".formatted(productMapper.resolveCategoryCode(slotKey)),
                ErrorCode.DATABASE_ERROR
            );
        }
        return categoryId;
    }
}
