package com.finalproject.coordi.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.domain.RecommendationResult;
import com.finalproject.coordi.recommendation.domain.ScoredRecommendationItem;
import com.finalproject.coordi.recommendation.domain.type.SlotKey;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import com.finalproject.coordi.recommendation.exception.AppException;
import com.finalproject.coordi.recommendation.exception.ErrorCode;
import com.finalproject.coordi.recommendation.persistence.RecommendationItemRecord;
import com.finalproject.coordi.recommendation.persistence.RecommendationMapper;
import com.finalproject.coordi.recommendation.persistence.RecommendationRecord;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PersistenceService {
    private static final Map<SlotKey, Long> SLOT_CATEGORY_ID = Map.of(
        SlotKey.TOPS, 1L,
        SlotKey.BOTTOMS, 2L,
        SlotKey.OUTERWEAR, 3L,
        SlotKey.SHOES, 4L,
        SlotKey.ACCESSORIES, 5L
    );

    private final RecommendationMapper recommendationMapper;
    private final ObjectMapper objectMapper;

    public PersistenceService(RecommendationMapper recommendationMapper, ObjectMapper objectMapper) {
        this.recommendationMapper = recommendationMapper;
        this.objectMapper = objectMapper;
    }

    // recommendation과 recommendation_item을 저장하고 recId를 반환한다.
    public Long persist(RecommendationRequest request, RecommendationResult result) {
        try {
            RecommendationRecord rec = RecommendationRecord.builder()
                .userId(1L)
                .inputMode("TEXT_IMAGE_WEATHER")
                .inputText(request.inputText())
                .productOption("SHOPPING")
                .isSaved(Boolean.FALSE)
                .aiBlueprint(toJsonString(result.aiBlueprint()))
                .aiExplanation("AI-First, Rule-Second workflow applied.")
                .build();
            recommendationMapper.insertRecommendation(rec);

            for (ScoredRecommendationItem scored : result.scoredItems()) {
                RecommendationItemRecord item = RecommendationItemRecord.builder()
                    .recId(rec.getRecId())
                    .slotKey(scored.draftItem().slotKey().code())
                    .sourceType("PRODUCT")
                    .productId(scored.productId())
                    .categoryId(SLOT_CATEGORY_ID.getOrDefault(scored.draftItem().slotKey(), 1L))
                    .itemName(scored.draftItem().itemName())
                    .searchQuery(scored.draftItem().searchQuery())
                    .attributesJson(toJsonString(scored.blueprintSlotNode().path("attributes")))
                    .tempMin(scored.draftItem().tempMin())
                    .tempMax(scored.draftItem().tempMax())
                    .priority(scored.draftItem().priority().code())
                    .selectionStage(scored.selectionStage().name())
                    .matchScore(scored.matchScore())
                    .styleScore(scored.styleScore())
                    .colorScore(scored.colorScore())
                    .tempScore(scored.tempScore())
                    .scoringDetailsJson(toJsonString(scored.blueprintSlotNode().path("scoring_details")))
                    .reason(scored.finalReasoning())
                    .build();
                recommendationMapper.insertRecommendationItem(item);
            }

            return rec.getRecId();
        } catch (Exception e) {
            throw new AppException(ErrorCode.DB_ERROR, "추천 결과 DB 저장에 실패했습니다.", e);
        }
    }

    // JSON 노드를 문자열로 변환한다.
    private String toJsonString(com.fasterxml.jackson.databind.JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("JSON serialize failed", e);
        }
    }
}
