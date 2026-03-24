package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;

/**
 * 프론트 요청을 payload 단계에서 다루는 내부 컨텍스트 모델이다.
 */
public record PayloadDto(
    String naturalText,
    GenderType gender,
    WeatherContext weather,
    ImageContext image
) {
    /**
     * payload 구성용 날씨 컨텍스트 모델이다.
     */
    public record WeatherContext(
        Double temperature,
        Double feelsLike,
        String weatherStatus,
        String rainProbability,
        String weatherSource
    ) {
    }

    /**
     * payload 구성용 이미지 컨텍스트 모델이다.
     */
    public record ImageContext(
        byte[] imageBytes,
        String mimeType
    ) {
    }
}
