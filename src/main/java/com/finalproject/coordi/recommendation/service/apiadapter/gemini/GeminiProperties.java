package com.finalproject.coordi.recommendation.service.apiadapter.gemini;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.api.gemini")
public class GeminiProperties {
    private String endpoint;
    private String model;
    private String key;
}
