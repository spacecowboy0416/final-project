package com.finalproject.coordi.recommendation.service.apiport;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.PromptPayload;

/**
 * AI Blueprint 생성을 위한 아웃바운드 포트.
 */
public interface AiPort {
    /**
     * 프롬프트 문자열과 업로드 이미지를 기반으로 AI blueprint JSON을 생성한다.
     */
    JsonNode generateBlueprint(PromptPayload promptPayload, BlueprintRequestDto.ImageData imageData);
}
