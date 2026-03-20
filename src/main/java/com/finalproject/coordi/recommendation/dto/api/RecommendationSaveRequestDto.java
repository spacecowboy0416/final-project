package com.finalproject.coordi.recommendation.dto.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 디버그 화면에서 이미 생성된 추천 결과를 재추천 없이 저장하기 위한 요청 DTO.
 */
public record RecommendationSaveRequestDto(
    @Valid @NotNull UserRequestDto request,
    @Valid @NotNull RecommendationDebugResponseDto debugResult
) {
}
