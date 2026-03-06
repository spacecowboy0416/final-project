package com.finalproject.coordi.recommendation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.DraftSource;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.RecommendationStatus;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SelectionStage;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.TpoType;
import java.util.List;

/**
 * 코디 추천 실행 응답 계약.
 */
public record CoordinationResponse(
    String coordinationId,
    RecommendationStatus status,
    DraftSource draftSource,
    TpoType tpoType,
    StyleMode styleMode,
    JsonNode aiBlueprint,
    List<CoordinationOutput> coordination,
    List<ItemResult> finalItems
) {
    public record ItemResult(
        SlotKey slotKey,
        String itemName,
        String searchQuery,
        String reasoning,
        PriorityType priority,
        SelectionStage selectionStage,
        double matchScore,
        ProductPreview product
    ) {
    }

    public record ProductPreview(
        String name,
        int price,
        String imageUrl,
        String link
    ) {
    }
}
