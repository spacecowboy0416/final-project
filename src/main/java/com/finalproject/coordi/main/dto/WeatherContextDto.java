package com.finalproject.coordi.main.dto;

import java.time.LocalDateTime;

import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.RainProbabilityType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐시 및 공통 재사용을 위한 내부 날씨 컨텍스트 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherContextDto {

    private String conditionText;
    private WeatherStatusType weatherStatus;
    private RainProbabilityType rainProbability;

    private double temperature;
    private double feelsLike;
    private int humidity;
    private double windMs;
    private double precipMm;

    private boolean todayRain;
    private String resolvedBy;

    private LocalDateTime recordedAt;

    public String getWeatherStatusCode() {
        return weatherStatus != null ? weatherStatus.code() : null;
    }

    public String getRainProbabilityCode() {
        return rainProbability != null ? rainProbability.code() : null;
    }
}