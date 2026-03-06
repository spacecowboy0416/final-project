package com.finalproject.coordi.recommendation.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.domain.BlueprintItem;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Gemini 응답 JSON을 내부 DRAFT 도메인 모델로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class DraftItemMapper {
    private static final List<SlotKey> SLOT_KEYS = List.of(
        SlotKey.TOPS, SlotKey.BOTTOMS, SlotKey.OUTERWEAR, SlotKey.SHOES, SlotKey.ACCESSORIES
    );

    private static final Map<SlotKey, String> SLOT_CATEGORY = Map.of(
        SlotKey.TOPS, "top",
        SlotKey.BOTTOMS, "pants",
        SlotKey.OUTERWEAR, "outerwear",
        SlotKey.SHOES, "shoes",
        SlotKey.ACCESSORIES, "accessory"
    );

    private final ObjectMapper objectMapper;

    /**
     * Gemini ai_blueprint.coordination 블록을 슬롯별 DRAFT 아이템 리스트로 파싱한다.
     */
    @SuppressWarnings("unchecked")
    public List<BlueprintItem> fromGeminiResponse(JsonNode geminiResponse) {
        JsonNode coordination = geminiResponse.path("ai_blueprint").path("coordination");
        if (coordination.isMissingNode() || !coordination.isObject()) {
            return List.of();
        }

        List<BlueprintItem> result = new ArrayList<>();
        for (SlotKey slotKey : SLOT_KEYS) {
            JsonNode slot = coordination.path(slotKey.code());
            if (slot.isMissingNode() || !slot.isObject()) {
                continue;
            }

            Map<String, Object> attributes = objectMapper.convertValue(slot.path("attributes"), Map.class);
            if (attributes == null) {
                attributes = new HashMap<>();
            }

            PriorityType priority = "optional".equalsIgnoreCase(slot.path("priority").asText())
                ? PriorityType.OPTIONAL
                : PriorityType.ESSENTIAL;

            result.add(new BlueprintItem(
                slotKey,
                slot.path("item_name").asText(slotKey.code() + " item"),
                slot.path("search_query").asText("남성 " + slotKey.code()),
                slot.path("category").asText(SLOT_CATEGORY.get(slotKey)),
                attributes,
                slot.path("temp_range").path(0).asInt(0),
                slot.path("temp_range").path(1).asInt(30),
                slot.path("reasoning").asText("추천 근거"),
                priority
            ));
        }
        return result;
    }
}

