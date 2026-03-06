package com.finalproject.coordi.recommendation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.BlueprintItem;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SelectionStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 점수 계산 결과를 ai_blueprint의 슬롯 JSON 구조로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class BlueprintSlotMapper {
    private final ObjectMapper objectMapper;

    /**
     * DRAFT 아이템과 점수 정보를 ai_blueprint 슬롯 노드로 변환한다.
     */
    public ObjectNode toSlotNode(
        BlueprintItem draft,
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

        ObjectNode appliedWeights = objectMapper.createObjectNode();
        appliedWeights.put("w1", 0.4);
        appliedWeights.put("w2", 0.3);
        appliedWeights.put("w3", 0.3);
        scoringDetails.set("applied_weights", appliedWeights);

        slotNode.set("scoring_details", scoringDetails);
        return slotNode;
    }
}

