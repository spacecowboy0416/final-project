package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import java.net.URI;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 외부 이미지 URL을 실제 바이트 배열(byte[])로 내려받는 컴포넌트다.
 *
 * 이 클래스를 따로 분리한 이유는 다음과 같다.
 * - 네트워크 다운로드 책임을 저장 로직에서 분리하기 위해
 * - MIME 추정, checksum 계산 같은 "이미지 다운로드 후처리"를 한 곳에 모으기 위해
 * - 나중에 다운로드 정책(타임아웃, 헤더, 재시도)이 바뀌어도 이 클래스만 수정하기 위해
 */
@Component
@RequiredArgsConstructor
public class ProductImageDownloader {
    private static final String DEFAULT_IMAGE_MIME_TYPE = MediaType.IMAGE_JPEG_VALUE;

    @Qualifier("productImageRestClient")
    private final RestClient productImageRestClient;

    /**
     * imageUrl을 호출해 실제 이미지 바이트를 가져온다.
     *
     * 여기서 ResponseEntity<byte[]>를 쓰는 이유:
     * - body로는 이미지 바이트를 받고
     * - header로는 Content-Type 같은 메타 정보를 함께 읽기 위해서다.
     *
     * 반환값은 DownloadedProductImage record인데,
     * "다운로드 결과를 한 번에 묶어서 넘기는 불변 데이터"라고 이해하면 된다.
     */
    public DownloadedProductImage download(String imageUrl) {
        ResponseEntity<byte[]> response = productImageRestClient.get()
            .uri(URI.create(imageUrl))
            .retrieve()
            .toEntity(byte[].class);

        byte[] imageBytes = response.getBody();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAIL);
        }

        String mimeType = response.getHeaders().getContentType() != null
            ? response.getHeaders().getContentType().toString()
            : inferMimeType(imageUrl);
        return new DownloadedProductImage(imageBytes, mimeType, checksumSha256(imageBytes));
    }

    /**
     * 서버가 Content-Type 헤더를 안 주는 경우를 대비한 fallback MIME 추정 로직이다.
     *
     * URL 문자열에 .png, .webp 같은 확장자가 들어 있는지 보고 대략적인 MIME 타입을 정한다.
     * 완벽한 방식은 아니지만, 헤더가 비었을 때의 최소 대응으로는 충분하다.
     */
    private String inferMimeType(String imageUrl) {
        String lowerCaseUrl = imageUrl == null ? "" : imageUrl.toLowerCase();
        if (lowerCaseUrl.contains(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (lowerCaseUrl.contains(".webp")) {
            return "image/webp";
        }
        if (lowerCaseUrl.contains(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        return DEFAULT_IMAGE_MIME_TYPE;
    }

    /**
     * 이미지 바이트의 SHA-256 checksum을 계산한다.
     *
     * checksum은 "이 파일의 내용 자체를 대표하는 지문"이라고 생각하면 쉽다.
     * 같은 바이트면 같은 checksum이 나오므로, 중복 확인이나 무결성 검증에 쓸 수 있다.
     *
     * 코드 포인트:
     * - MessageDigest.getInstance("SHA-256"): SHA-256 알고리즘 준비
     * - digest(...): 실제 해시 계산
     * - String.format("%02x", value): 바이트를 2자리 16진수 문자열로 바꿈
     */
    private String checksumSha256(byte[] imageBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(imageBytes);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new BusinessException(
                "상품 이미지 checksum 계산 실패 [%s]".formatted(exception.getMessage()),
                ErrorCode.INTERNAL_SERVER_ERROR,
                exception
            );
        }
    }

    /**
     * 다운로드 결과를 묶는 작은 record다.
     *
     * record 문법은
     * - 생성자
     * - getter 성격의 accessor
     * - equals/hashCode/toString
     * 을 자동으로 제공하는 "데이터 전용 타입"이다.
     *
     * 초보자 관점에서는 "DTO를 아주 짧게 쓰는 문법"으로 이해하면 충분하다.
     */
    public record DownloadedProductImage(
        byte[] imageBytes,
        String mimeType,
        String checksumSha256
    ) {
    }
}


