package com.finalproject.coordi.recommendation.infra.weather;

import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.RainProbabilityType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;

/**
 * Redis 캐시 날씨 JSON 역직렬화 전용 DTO.
 */
public record RedisWeatherDto(
    Double temperature,
    Double feelsLike,
    WeatherStatusType weatherStatus,
    RainProbabilityType rainProbability
) {
}
