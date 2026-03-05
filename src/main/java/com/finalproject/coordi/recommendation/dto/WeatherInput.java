package com.finalproject.coordi.recommendation.dto;

import com.finalproject.coordi.recommendation.domain.type.PrecipitationType;
import jakarta.validation.constraints.NotNull;

/**
 * 팀 공통 날씨 입력 계약 DTO.
 */
public record WeatherInput(
    @NotNull Double temp,
    String conditionText,
    Integer humidity,
    Double windSpeed,
    PrecipitationType precipitationType,
    Integer precipitationProb
) {
}
