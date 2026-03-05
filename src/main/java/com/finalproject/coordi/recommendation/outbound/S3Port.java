package com.finalproject.coordi.recommendation.outbound;

/**
 * S3 업로드 아웃바운드 포트.
 */
public interface S3Port {
    /**
     * 원본 URL 이미지를 S3에 업로드하고 접근 URL을 반환한다.
     */
    String uploadImage(S3UploadRequest request);
}
