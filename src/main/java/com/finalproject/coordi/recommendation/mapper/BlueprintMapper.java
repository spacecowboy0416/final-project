package com.finalproject.coordi.recommendation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.RequestContext;
import com.finalproject.coordi.recommendation.domain.SearchScoredItem;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.ItemCategoryType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 오케스트레이션 결과를 ai_blueprint 최종 JSON 구조로 조립한다.
 */
@Component
@RequiredArgsConstructor
public class BlueprintMapper {
    private final ObjectMapper objectMapper;

    /**
     * 컨텍스트와 슬롯 결과 목록으로 ai_blueprint 루트 JSON을 생성한다.
     */
    public ObjectNode toBlueprintRoot(RequestContext context, List<SearchScoredItem> scoredItems) {
        ObjectNode coordination = objectMapper.createObjectNode();
        for (SearchScoredItem item : scoredItems) {
            coordination.set(item.blueprintItem().slotKey().code(), item.blueprintSlotNode());
        }

        ObjectNode blueprint = objectMapper.createObjectNode();
        blueprint.put("schemaVersion", "1.0.0");
        blueprint.set("main_item_analysis", toMainItemAnalysis(context));
        blueprint.set("coordination", coordination);
        blueprint.put("styling_rule_applied", "톤온톤 배색 + 상하 실루엣 밸런스");
        blueprint.put("workflow", "AI-First, Rule-Second");
        blueprint.put("final_threshold", 70.0);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("ai_blueprint", blueprint);
        return root;
    }

    /**
     * 추천 컨텍스트를 main_item_analysis 블록으로 변환한다.
     */
    private ObjectNode toMainItemAnalysis(RequestContext context) {
        ObjectNode node = objectMapper.createObjectNode();
        int low = (int) Math.floor(context.currentTemp() - 2);
        int high = (int) Math.ceil(context.currentTemp() + 2);
        node.put("temp", low + "-" + high + "C");
        node.put("season", context.season().code());
        node.put("color", ColorType.NAVY.code());
        node.put("type", ItemCategoryType.JACKET.code());
        node.put("style", context.styleMode().code());
        return node;
    }
}
