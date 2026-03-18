package com.finalproject.coordi.recommendation.infra.weather;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "external.api.weather.redis")
public class WeatherRedisProperties {
    private String keyPrefix;
}

