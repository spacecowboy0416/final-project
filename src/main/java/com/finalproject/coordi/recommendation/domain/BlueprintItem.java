package com.finalproject.coordi.recommendation.domain;

import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import java.util.Map;

/**
 * AI가 제안한 DRAFT 슬롯 아이템을 표현한다.
 */
public record BlueprintItem(
    SlotKey slotKey,
    String itemName,
    String searchQuery,
    String category,
    Map<String, Object> attributes,
    int tempMin,
    int tempMax,
    String reasoning,
    PriorityType priority
) {
}
