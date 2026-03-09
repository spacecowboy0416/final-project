package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import org.springframework.stereotype.Component;

@Component
public class BlueprintGenerator {
    public JsonNode generate(BlueprintRequestDto request, String prompt) {
        // TODO: AI 어댑터 호출로 블루프린트 원본 JSON을 생성한다.
        return null;
    }
}

