package com.finalproject.coordi.recommendation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.type.DraftSource;
import com.finalproject.coordi.recommendation.domain.type.PriorityType;
import com.finalproject.coordi.recommendation.domain.type.RecommendationStatus;
import com.finalproject.coordi.recommendation.domain.type.SelectionStage;
import com.finalproject.coordi.recommendation.domain.type.SlotKey;
import com.finalproject.coordi.recommendation.domain.type.StyleMode;
import com.finalproject.coordi.recommendation.domain.type.TpoType;
import java.util.List;

/**
 * 추천 실행 응답 DTO.
 */
public record RecommendationResponse(
    String recId,
    RecommendationStatus status,
    DraftSource draftSource,
    TpoType tpoType,
    StyleMode styleMode,
    WeatherInput weather,
    JsonNode aiBlueprint,
    List<CoordinationOutput> coordination,
    List<ItemResult> finalItems
) {
    /**
     * 슬롯별 최종 아이템 응답 모델.
     */
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

    /**
     * 상품 미리보기 응답 모델.
     */
    public record ProductPreview(
        String name,
        int price,
        String imageUrl,
        String link
    ) {
    }
}
