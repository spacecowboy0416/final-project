package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class BlueprintValidator {
    /**
     * Gemini가 반환한 blueprint JSON을 recommendation 파이프라인에서 계속 사용할 수 있게
     * 최소 구조만 확인한 뒤 그대로 다음 단계로 넘긴다.
     */
    public JsonNode validate(JsonNode rawBlueprintJson) {
        if (rawBlueprintJson == null || rawBlueprintJson.isMissingNode() || rawBlueprintJson.isNull()) {
            throw new IllegalStateException("AI blueprint 응답이 비어 있습니다.");
        }

        JsonNode blueprintRoot = rawBlueprintJson.path("ai_blueprint");
        if (!blueprintRoot.isObject()) {
            throw new IllegalStateException("AI blueprint 최상위 키 ai_blueprint가 없습니다.");
        }

        return rawBlueprintJson;
    }
}

