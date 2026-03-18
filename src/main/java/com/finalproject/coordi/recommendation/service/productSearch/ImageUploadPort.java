package com.finalproject.coordi.recommendation.service.productSearch;

/**
 * 사진 업로드를 위한 아웃바운드 포트.
 */
public interface ImageUploadPort {

    // 원본 이미지를 외부 저장소(S3 등)에 업로드하고 접근 URL을 반환한다.
    UploadedImage uploadImage(ImageUploadRequest request);

    /**
     * 업로드 요청 파라미터.
     * @param imageBytes 원본 이미지 바이너리
     * @param mimeType 원본 이미지 MIME 타입
     * @param storagePath 저장될 폴더 경로 (예: "profiles", "recommendations")
     */
    record ImageUploadRequest(
        byte[] imageBytes,
        String mimeType,
        String storagePath
    ) {
    }

    /**
     * 업로드 완료 후 저장소 관점의 메타데이터를 반환한다.
     */
    record UploadedImage(
        String storageProvider,
        String storageUrl,
        String storagePath,
        String mimeType,
        int imageSizeBytes
    ) {
    }
}



