package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;

/**
 * blueprint 출력 계약 DTO.
 */
public record BlueprintOutputDto(
    CategoryType slotKey,
    String itemName,
    String searchQuery,
    double matchScore,
    String color,
    String material,
    String fit,
    String style
) {
}


