package com.finalproject.coordi.recommendation.dto;

import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SelectionStage;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;

/**
 * 팀 공통 코디 출력 계약 DTO.
 */
public record CoordinationOutput(
    SlotKey slotKey,
    String itemName,
    String searchQuery,
    SelectionStage selectionStage,
    double matchScore,
    String color,
    String material,
    String fit,
    String style
) {
}
