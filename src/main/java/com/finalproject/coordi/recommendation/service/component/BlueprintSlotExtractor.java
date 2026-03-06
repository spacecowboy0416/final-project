package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 검증된 ai_blueprint를 슬롯 단위의 내부 작업 모델로 변환해, 오케스트레이터가 파싱 책임 없이 검색/매핑 책임만 수행하도록 분리한다.
 */
@Component
public class BlueprintSlotExtractor {
    public List<SlotDraft> extract(JsonNode validatedBlueprintRoot) {
        JsonNode coordination = validatedBlueprintRoot.path("ai_blueprint").path("coordination");
        List<SlotDraft> result = new ArrayList<>();
        for (SlotKey slotKey : SlotKey.values()) {
            JsonNode slotNode = coordination.path(slotKey.code());
            JsonNode attributes = slotNode.path("attributes");
            result.add(
                new SlotDraft(
                    slotKey,
                    textOrEmpty(slotNode, "item_name"),
                    textOrEmpty(slotNode, "search_query"),
                    textOrEmpty(slotNode, "reasoning"),
                    parsePriority(textOrEmpty(slotNode, "priority")),
                    textOrEmpty(attributes, "color"),
                    textOrEmpty(attributes, "material"),
                    textOrEmpty(attributes, "fit"),
                    textOrEmpty(attributes, "style")
                )
            );
        }
        return result;
    }

    private PriorityType parsePriority(String rawPriority) {
        return "essential".equalsIgnoreCase(rawPriority) ? PriorityType.ESSENTIAL : PriorityType.OPTIONAL;
    }

    private String textOrEmpty(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isTextual() ? value.asText() : "";
    }

    public record SlotDraft(
        SlotKey slotKey,
        String itemName,
        String searchQuery,
        String reasoning,
        PriorityType priority,
        String color,
        String material,
        String fit,
        String style
    ) {
    }
}
