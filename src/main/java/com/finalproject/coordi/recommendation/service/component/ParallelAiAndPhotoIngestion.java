package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.api.CoordinationRequestDto;
import org.springframework.stereotype.Component;

/**
 * 1단계 스켈레톤: AI 코디 생성 + 사진/메타데이터 DB 저장 병렬 처리.
 */
@Component
public class ParallelAiAndPhotoIngestion {
    public StageOneResult execute(CoordinationRequestDto request) {
        throw new UnsupportedOperationException("TODO: AI 생성 + 사진/메타데이터 저장 병렬 구현");
    }

    public record StageOneResult(
        JsonNode aiRawBlueprint,
        Long photoAssetId
    ) {
    }
}
