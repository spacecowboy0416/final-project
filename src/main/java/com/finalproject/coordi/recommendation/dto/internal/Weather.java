package com.finalproject.coordi.recommendation.dto.internal;

public record Weather(
    Double temperature,
    Double feelsLike,
    String weatherStatus,
    String rainProbability,
    String weatherSource
) {
}
