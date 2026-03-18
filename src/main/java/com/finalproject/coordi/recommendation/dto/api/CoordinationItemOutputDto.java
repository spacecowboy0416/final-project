package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;

/**
 * 최종 coordination 응답의 단일 아이템 계약 DTO.
 */
public record CoordinationItemOutputDto(
    CategoryType slotKey,
    String itemName,
    String imageUrl,
    String brandName,
    Integer salePrice,
    String productDetailUrl,
    ItemCategoryType category,
    double matchScore,
    Integer tempMin,
    Integer tempMax,
    PriorityType priority,
    String reasoning,
    ColorType color,
    MaterialType material,
    FitType fit,
    StyleType style
) {
}

