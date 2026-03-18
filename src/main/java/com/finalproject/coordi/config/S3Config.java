package com.finalproject.coordi.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 세팅이 완료될 때까지 @Configuration 어노테이션을 주석 처리하여 비활성화
// @Configuration
public class S3Config {

    // application.yml 파일에 작성해둔 AWS Access Key를 불러옵니다.
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    // application.yml 파일에 작성해둔 AWS Secret Key를 불러옵니다.
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;
    
    // application.yml 파일에 작성해둔 AWS 지역(Region) 정보를 불러옵니다.
    @Value("${cloud.aws.region.static}")
    private String region;

    // AmazonS3 객체를 스프링 Bean으로 등록하여 S3UploadService 등에서 주입받아 사용할 수 있게 합니다.
    // @Bean
    public AmazonS3 amazonS3Client() {
        // 1. 가져온 키 값으로 AWS 자격 증명(Credential) 객체를 생성합니다.
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // 2. 지역(Region)과 자격 증명을 묶어서 AmazonS3 클라이언트를 빌드 후 반환합니다.
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }
}