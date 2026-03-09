package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.dto.internal.BlueprintSlot;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;

/**
 * 팀 공통 코디 출력 계약 DTO.
 */
public record CoordinationOutputDto(
    SlotKey slotKey,
    String itemName,
    String searchQuery,
    double matchScore,
    String color,
    String material,
    String fit,
    String style
) {
    public static CoordinationOutputDto from(BlueprintSlot slotBlueprint, double matchScore) {
        return new CoordinationOutputDto(
            slotBlueprint.slotKey(),
            slotBlueprint.itemName(),
            slotBlueprint.searchQuery(),
            matchScore,
            slotBlueprint.color(),
            slotBlueprint.material(),
            slotBlueprint.fit(),
            slotBlueprint.style()
        );
    }
}
