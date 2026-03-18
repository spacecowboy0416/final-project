package com.finalproject.coordi.recommendation.infra.s3;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.recommendation.service.productSearch.ImageUploadPort;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Adapter implements ImageUploadPort {
    private final S3Client s3Client;
    private final S3Properties imageStorageProperties;

    @Override
    public UploadedImage uploadImage(ImageUploadRequest request) {
        validate(request);

        String objectKey = buildObjectKey(request.mimeType(), request.storagePath());
        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(imageStorageProperties.getBucket())
                    .key(objectKey)
                    .contentType(request.mimeType())
                    .contentLength((long) request.imageBytes().length)
                    .build(),
                RequestBody.fromBytes(request.imageBytes())
            );

            String imageUrl = buildImageUrl(objectKey);
            log.info(
                "recommendation image uploaded bucket={} key={} size={} mimeType={}",
                imageStorageProperties.getBucket(),
                objectKey,
                request.imageBytes().length,
                request.mimeType()
            );
            return new UploadedImage(
                "S3",
                imageUrl,
                objectKey,
                request.mimeType(),
                request.imageBytes().length
            );
        } catch (Exception exception) {
            throw new BusinessException(
                "S3 이미지 업로드 실패 [bucket=%s, key=%s]".formatted(imageStorageProperties.getBucket(), objectKey),
                ErrorCode.IMAGE_UPLOAD_FAIL,
                exception
            );
        }
    }

    private void validate(ImageUploadRequest request) {
        if (request == null
            || request.imageBytes() == null
            || request.imageBytes().length == 0
            || !StringUtils.hasText(request.mimeType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String buildObjectKey(String mimeType, String storagePath) {
        LocalDate today = LocalDate.now();
        String normalizedPath = StringUtils.hasText(storagePath)
            ? trimSlashes(storagePath)
            : imageStorageProperties.normalizedRecommendationPath();
        // 날짜별 prefix를 두어 업로드 키 분산과 운영 중 탐색을 쉽게 유지한다.
        return "%s/%d/%02d/%02d/%s%s".formatted(
            normalizedPath,
            today.getYear(),
            today.getMonthValue(),
            today.getDayOfMonth(),
            UUID.randomUUID(),
            resolveExtension(mimeType)
        );
    }

    private String buildImageUrl(String objectKey) {
        String configuredBaseUrl = imageStorageProperties.normalizedBaseUrl();
        if (StringUtils.hasText(configuredBaseUrl)) {
            return configuredBaseUrl + "/" + objectKey;
        }
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(
            imageStorageProperties.getBucket(),
            imageStorageProperties.getRegion(),
            objectKey
        );
    }

    private String resolveExtension(String mimeType) {
        if (!StringUtils.hasText(mimeType)) {
            return imageStorageProperties.normalizedExtension();
        }

        return switch (mimeType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> imageStorageProperties.normalizedExtension();
        };
    }

    private String trimSlashes(String path) {
        return path.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}

