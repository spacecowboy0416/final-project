package com.finalproject.coordi.recommendation.infra.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.storage.image")
public class S3Properties {
    private String bucket;
    private String region = "ap-northeast-2";
    private String accessKey;
    private String secretKey;
    private String baseUrl;
    private String extension = ".jpg";
    private String recommendationPath = "recommendations";

    public String normalizedBaseUrl() {
        if (!StringUtils.hasText(baseUrl)) {
            return null;
        }
        return baseUrl.replaceAll("/+$", "");
    }

    public String normalizedExtension() {
        if (!StringUtils.hasText(extension)) {
            return ".jpg";
        }
        return extension.startsWith(".") ? extension : "." + extension;
    }

    public String normalizedRecommendationPath() {
        if (!StringUtils.hasText(recommendationPath)) {
            return "recommendations";
        }
        return recommendationPath.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}

