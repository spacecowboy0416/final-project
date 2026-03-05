package com.finalproject.coordi.recommendation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 추천 실행 요청 DTO.
 */
public record RecommendationRequest(
    @NotBlank String inputText,
    @NotBlank String imageUrl,
    @Valid @NotNull WeatherInput weather,
    @Valid @NotNull LocationInput location,
    List<String> userTags
) {
}
