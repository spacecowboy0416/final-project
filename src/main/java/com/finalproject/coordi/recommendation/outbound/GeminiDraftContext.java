package com.finalproject.coordi.recommendation.outbound;

public record GeminiDraftContext(
    double currentTemp,
    String styleMode,
    String season
) {
}
