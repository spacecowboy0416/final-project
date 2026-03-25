package com.finalproject.coordi.closet.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;

    // 환경 설정 파일(application.yml)에 명시된 목표 S3 버킷 명칭을 로드 (경로 누락 방지 기본값 세팅)
    @Value("${cloud.aws.s3.bucket:${AWS_S3_BUCKET:aws_bucket_name}}")
    private String bucket;

    // 전달받은 파일 객체를 AWS S3 저장소에 전송하고, 접근 가능한 퍼블릭 URL을 반환
    public String uploadImage(MultipartFile file) {
        // 업로드된 파일의 유효성을 검증하며, 빈 객체일 경우 예외를 방지하기 위해 널 처리
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일명 중복으로 인한 데이터 덮어쓰기 현상을 방지하기 위해 UUID가 결합된 고유 식별 명칭을 생성
        String originalFilename = file.getOriginalFilename();
        String storeFileName = "closet/" + UUID.randomUUID().toString() + "_" + originalFilename;

        // 클라우드 스토리지 상에서 파일 형식을 올바르게 식별하도록 크기와 MIME 타입 명시적 메타데이터
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            // AmazonS3 클라이언트를 활용하여 지정된 버킷 경로로 스트림 데이터를 전송
            amazonS3.putObject(bucket, storeFileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            // 전송 오류 발생 시 시스템 로그를 기록하고, 후속 로직의 트랜잭션을 롤백하기 위해 런타임 예외 발생
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }

        // 프론트엔드 환경에서 해당 자원에 즉시 접근할 수 있도록 전체 경로(Absolute URL)를 반환
        return amazonS3.getUrl(bucket, storeFileName).toString();
    }
}