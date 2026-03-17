package com.finalproject.coordi.recommendation.service.apiadapter.naver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.api.shopping")
public class NaverShoppingProperties {
    private String endpoint;
    private String clientId;
    private String clientSecret;
}
