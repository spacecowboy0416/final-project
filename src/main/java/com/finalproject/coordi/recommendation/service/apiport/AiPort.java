package com.finalproject.coordi.recommendation.service.apiport;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;

/**
 * AI Blueprint 생성을 위한 아웃바운드 포트.
 */
public interface AiPort {
    // 요청 DTO를 기반으로 AI blueprint JSON을 생성한다.
    JsonNode generateBlueprint(BlueprintRequestDto request);
}
