package com.finalproject.coordi.recommendation.infra.map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.api.kakao-map")
public class KakaoMapProperties {
    private String endpoint;
    private String jsKey;
    private String adminKey;
    private String authScheme;
}

