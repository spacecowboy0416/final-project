package com.finalproject.coordi.recommendation.infra.gemini;

import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.api.gemini")
public class GeminiProperties {
    private String endpoint;
    private String model;
    private String key;

    @PostConstruct
    void validate() {
        if (!StringUtils.hasText(model) || !StringUtils.hasText(key)) {
            throw new RecommendationException.AdapterException(
                "Gemini 설정이 누락되었습니다. model/key를 확인하세요.",
                ErrorCode.RECOMMENDATION_GEMINI_ENDPOINT_MISCONFIGURED
            );
        }
        if (StringUtils.hasText(endpoint) && (endpoint.contains("/v1") || endpoint.contains("/v1beta"))) {
            throw RecommendationException.geminiEndpointMisconfigured(endpoint);
        }
    }
}

