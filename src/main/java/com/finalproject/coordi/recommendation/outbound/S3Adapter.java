package com.finalproject.coordi.recommendation.outbound;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3Adapter implements S3Port {
    private static final Logger log = LoggerFactory.getLogger(S3Adapter.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final String region;
    private final S3Client s3Client;

    public S3Adapter(
        @Value("${cloud.aws.credentials.access-key:}") String accessKey,
        @Value("${cloud.aws.credentials.secret-key:}") String secretKey,
        @Value("${cloud.aws.s3.bucket:}") String bucket,
        @Value("${cloud.aws.region.static:ap-northeast-2}") String region
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.region = region;
        this.s3Client = createClient(accessKey, secretKey, region);
    }

    // 원본 이미지 URL을 S3로 업로드하고 S3 URL을 반환한다. 실패 시 원본 URL을 반환한다.
    @Override
    public String uploadImage(S3UploadRequest request) {
        if (request == null || request.sourceUrl() == null || request.sourceUrl().isBlank()) {
            return null;
        }
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank() || bucket == null || bucket.isBlank()) {
            return request.sourceUrl();
        }
        if (s3Client == null) {
            return request.sourceUrl();
        }

        try {
            byte[] imageBytes = downloadBytes(request.sourceUrl());
            String objectKey = buildObjectKey(request);
            String contentType = detectContentType(request.sourceUrl());

            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;
        } catch (Exception e) {
            log.warn("S3 업로드 실패. sourceUrl={}, bucket={}, reason={}", request.sourceUrl(), bucket, e.getMessage());
            return request.sourceUrl();
        }
    }

    // URL에서 바이트 배열을 다운로드한다.
    private byte[] downloadBytes(String sourceUrl) throws Exception {
        URI uri = URI.create(sourceUrl);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "coordi-s3-uploader/1.0");
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("이미지 다운로드 실패. status=" + status);
        }
        try (InputStream is = connection.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    // 날짜/UUID 기반으로 중복 없는 object key를 생성한다.
    private String buildObjectKey(S3UploadRequest request) {
        LocalDate d = LocalDate.now();
        String ext = guessExtension(request.sourceUrl());
        String prefix = request.objectPrefix() == null || request.objectPrefix().isBlank() ? "recommendation" : request.objectPrefix();
        return prefix + "/" + d.getYear() + "/" + d.getMonthValue() + "/" + d.getDayOfMonth() + "/" + UUID.randomUUID() + ext;
    }

    // URL로부터 확장자를 추정한다.
    private String guessExtension(String url) {
        String lower = extractPath(url).toLowerCase();
        if (lower.contains(".png")) return ".png";
        if (lower.contains(".webp")) return ".webp";
        if (lower.contains(".gif")) return ".gif";
        return ".jpg";
    }

    // 파일 확장자 기준으로 content-type을 추정한다.
    private String detectContentType(String url) {
        String ext = guessExtension(url);
        return switch (ext) {
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            default -> "image/jpeg";
        };
    }

    // 입력 URL에서 path 부분만 추출한다.
    private String extractPath(String url) {
        try {
            return URI.create(url).getPath();
        } catch (Exception ignored) {
            return url;
        }
    }

    // S3 클라이언트를 초기화한다.
    private S3Client createClient(String accessKey, String secretKey, String region) {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            return null;
        }
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .build();
    }
}
