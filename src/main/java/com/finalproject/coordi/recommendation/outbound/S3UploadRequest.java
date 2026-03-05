package com.finalproject.coordi.recommendation.outbound;

/**
 * S3 업로드 요청 파라미터를 보관한다.
 */
public record S3UploadRequest(
    String sourceUrl,
    String objectPrefix
) {
}
