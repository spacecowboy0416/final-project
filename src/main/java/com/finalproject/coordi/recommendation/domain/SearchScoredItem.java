package com.finalproject.coordi.recommendation.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SelectionStage;

/**
 * 점수 계산 이후 확정 단계가 포함된 슬롯 아이템을 표현한다.
 */
public record SearchScoredItem(
    BlueprintItem blueprintItem,
    Long productId,
    SelectionStage selectionStage,
    double matchScore,
    double styleScore,
    double colorScore,
    double tempScore,
    String finalReasoning,
    SearchProductSnapshot searchProductSnapshot,
    ObjectNode blueprintSlotNode
) {
}
