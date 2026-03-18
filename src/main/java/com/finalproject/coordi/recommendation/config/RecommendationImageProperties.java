package com.finalproject.coordi.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation.image")
public class RecommendationImageProperties {
    private DataSize maxSize = DataSize.ofMegabytes(5);
}
