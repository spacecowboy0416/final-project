package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import com.finalproject.coordi.recommendation.service.apiport.AiPort;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.PromptPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlueprintGenerator {
    private final AiPort aiPort;

    /**
     * PromptBuilder가 만든 프롬프트와 업로드 이미지를 Gemini에 전달해 blueprint 원본 JSON을 생성한다.
     */
    public JsonNode generate(BlueprintRequestDto request, PromptPayload promptPayload) {
        return aiPort.generateBlueprint(promptPayload, request.imageData());
    }
}

