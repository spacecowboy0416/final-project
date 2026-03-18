package com.finalproject.coordi.closet.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
// @Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // AWS S3에 파일을 업로드하고 접근 가능한 URL 주소를 반환하는 메서드
    public String uploadImage(MultipartFile file) {
        // 원본 파일명 추출 및 중복 방지를 위한 UUID 추가
        String originalFilename = file.getOriginalFilename();
        String storeFileName = "closet/" + UUID.randomUUID().toString() + "_" + originalFilename;

        // S3에 올릴 파일의 메타데이터(크기, 확장자 등) 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            // 실제 S3 버킷으로 파일 전송
            amazonS3.putObject(bucket, storeFileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }

        // 업로드된 파일의 실제 S3 URL 주소 반환
        return amazonS3.getUrl(bucket, storeFileName).toString();
    }
}