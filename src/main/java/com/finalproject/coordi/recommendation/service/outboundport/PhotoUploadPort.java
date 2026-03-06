package com.finalproject.coordi.recommendation.service.outboundport;

/**
 * 사진 업로드를 위한 아웃바운드 포트.
 */
public interface PhotoUploadPort {

    /**
     * 원본 URL 이미지를 외부 저장소(S3 등)에 업로드하고 접근 가능한 URL을 반환한다.
     */
    String uploadImage(PhotoUploadRequest request);

    /**
     * 업로드 요청 파라미터.
     * @param sourceUrl 원본 이미지 경로
     * @param storagePath 저장될 폴더 경로 (예: "profiles", "recommendations")
     */
    record PhotoUploadRequest(
        String sourceUrl,
        String storagePath
    ) {
    }
}