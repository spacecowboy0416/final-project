package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.exception.AppException;
import com.finalproject.coordi.recommendation.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Gemini 출력이 ai_blueprint 계약을 만족하는지 검증한다.
 */
@Component
public class CoordinationValidator {
    private static final List<String> REQUIRED_SLOTS = List.of(
        "tops", "bottoms", "outerwear", "shoes", "accessories"
    );

    /**
     * 필수 키/형식을 검증하고 통과한 JSON을 그대로 반환한다.
     */
    public JsonNode validate(JsonNode root) {
        JsonNode blueprint = root.path("ai_blueprint");
        if (!blueprint.isObject()) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "ai_blueprint 객체가 없습니다.");
        }

        requireText(blueprint, "schemaVersion");
        requireObject(blueprint, "main_item_analysis");
        requireText(blueprint, "styling_rule_applied");

        JsonNode coordination = blueprint.path("coordination");
        if (!coordination.isObject()) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "coordination 객체가 없습니다.");
        }
        for (String slot : REQUIRED_SLOTS) {
            JsonNode slotNode = coordination.path(slot);
            if (!slotNode.isObject()) {
                throw new AppException(ErrorCode.LLM_PARSE_FAILED, "필수 슬롯 누락: " + slot);
            }
            requireText(slotNode, "item_name");
            requireText(slotNode, "search_query");
            requireText(slotNode, "category");
            requireObject(slotNode, "attributes");
            requireText(slotNode, "reasoning");
            requireText(slotNode, "priority");
            JsonNode tempRange = slotNode.path("temp_range");
            if (!tempRange.isArray() || tempRange.size() != 2) {
                throw new AppException(ErrorCode.LLM_PARSE_FAILED, slot + ".temp_range 형식이 잘못되었습니다.");
            }
        }
        return root;
    }

    private void requireText(JsonNode node, String key) {
        if (!node.path(key).isTextual() || node.path(key).asText().isBlank()) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "필수 문자열 키 누락: " + key);
        }
    }

    private void requireObject(JsonNode node, String key) {
        if (!node.path(key).isObject()) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "필수 객체 키 누락: " + key);
        }
    }
}

