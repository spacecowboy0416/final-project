package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.internal.AiBlueprintConstraintDto;
import org.springframework.stereotype.Component;

/**
 * 1-1단계 스켈레톤: AI 출력의 제약(스키마/필수 키/형식)을 검증한다.
 */
@Component
public class CoordinationValidator {
    public AiBlueprintConstraintDto.Envelope validateConstraints(JsonNode aiRawBlueprint) {
        throw new UnsupportedOperationException("TODO: 제약(스키마/필수 키/형식) 검증 구현");
    }
}
