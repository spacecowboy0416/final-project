package com.finalproject.coordi.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.DraftRecommendationItem;
import com.finalproject.coordi.recommendation.domain.RecommendationContext;
import com.finalproject.coordi.recommendation.domain.ScoredRecommendationItem;
import com.finalproject.coordi.recommendation.domain.type.PriorityType;
import com.finalproject.coordi.recommendation.domain.type.SelectionStage;
import org.springframework.stereotype.Component;

@Component
public class ScoringService {
    private static final double FINAL_THRESHOLD = 70.0;
    private final ObjectMapper objectMapper;

    public ScoringService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // S_match 수식 기반으로 점수를 계산하고 확정 단계를 부여한다.
    public ScoredRecommendationItem score(DraftRecommendationItem draft, RecommendationContext context) {
        double styleScore = computeStyleScore(draft, context.styleMode());
        double colorScore = computeColorScore(draft);
        double tempScore = computeTempScore(draft, context.currentTemp());
        double matchScore = Math.round((styleScore * 0.4 + colorScore * 0.3 + tempScore * 0.3) * 100.0) / 100.0;
        SelectionStage selectionStage = matchScore >= FINAL_THRESHOLD ? SelectionStage.FINAL : SelectionStage.REJECTED;

        ObjectNode slotNode = buildBlueprintSlotNode(draft, styleScore, colorScore, tempScore, matchScore, selectionStage);
        String finalReasoning = SelectionStage.FINAL.equals(selectionStage)
            ? draft.reasoning()
            : draft.reasoning() + " (점수 미달로 반려, 보정 필요)";

        return new ScoredRecommendationItem(
            draft,
            null,
            selectionStage,
            matchScore,
            styleScore,
            colorScore,
            tempScore,
            finalReasoning,
            null,
            slotNode
        );
    }

    // 슬롯별 blueprint 노드를 구성한다.
    private ObjectNode buildBlueprintSlotNode(
        DraftRecommendationItem draft,
        double styleScore,
        double colorScore,
        double tempScore,
        double matchScore,
        SelectionStage selectionStage
    ) {
        ObjectNode slotNode = objectMapper.createObjectNode();
        slotNode.put("item_name", draft.itemName());
        slotNode.put("search_query", draft.searchQuery());
        slotNode.put("category", draft.category());
        slotNode.set("attributes", objectMapper.valueToTree(draft.attributes()));
        ArrayNode tempRange = objectMapper.createArrayNode();
        tempRange.add(draft.tempMin());
        tempRange.add(draft.tempMax());
        slotNode.set("temp_range", tempRange);
        slotNode.put("reasoning", draft.reasoning());
        slotNode.put("priority", draft.priority().code());
        slotNode.put("selection_stage", selectionStage.name());
        slotNode.put("match_score", matchScore);

        ObjectNode scoringDetails = objectMapper.createObjectNode();
        scoringDetails.put("style_score", styleScore);
        scoringDetails.put("color_score", colorScore);
        scoringDetails.put("temp_score", tempScore);
        ObjectNode weights = objectMapper.createObjectNode();
        weights.put("w1", 0.4);
        weights.put("w2", 0.3);
        weights.put("w3", 0.3);
        scoringDetails.set("applied_weights", weights);
        slotNode.set("scoring_details", scoringDetails);
        return slotNode;
    }

    // 스타일 적합 점수를 계산한다.
    private double computeStyleScore(DraftRecommendationItem draft, com.finalproject.coordi.recommendation.domain.type.StyleMode styleMode) {
        String style = String.valueOf(draft.attributes().getOrDefault("style", "comfortable"));
        double base = style.equals(styleMode.code()) ? 92.0 : 75.0;
        if (PriorityType.ESSENTIAL.equals(draft.priority())) {
            base += 2.0;
        }
        return Math.min(100.0, base);
    }

    // 색상 조화 점수를 계산한다.
    private double computeColorScore(DraftRecommendationItem draft) {
        String color = String.valueOf(draft.attributes().getOrDefault("color", "unknown"));
        if ("navy".equals(color) || "gray".equals(color)) {
            return 90.0;
        }
        if ("black".equals(color) || "white".equals(color)) {
            return 84.0;
        }
        return 65.0;
    }

    // 온도 적합 점수를 계산한다.
    private double computeTempScore(DraftRecommendationItem draft, double currentTemp) {
        if (currentTemp >= draft.tempMin() && currentTemp <= draft.tempMax()) {
            return 92.0;
        }
        double diff = Math.min(Math.abs(currentTemp - draft.tempMin()), Math.abs(currentTemp - draft.tempMax()));
        return Math.max(50.0, 92.0 - diff * 6.0);
    }

}
