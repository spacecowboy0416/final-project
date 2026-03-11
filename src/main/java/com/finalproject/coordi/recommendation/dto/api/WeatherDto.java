package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.RainProbabilityType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;

/**
 * Redis에 캐싱된 날씨 JSON을 recommendation 내부 타입으로 역직렬화하기 위한 DTO.
 * Redis 캐시 JSON은 recommendation 팀이 합의한 camelCase 필드명만 허용한다.
 */
public record WeatherDto(
    Double temperature,
    Double feelsLike,
    WeatherStatusType weatherStatus,
    RainProbabilityType rainProbability
) {
}
