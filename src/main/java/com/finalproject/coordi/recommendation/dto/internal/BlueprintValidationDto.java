package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import java.util.Map;

public final class BlueprintValidationDto {
    private BlueprintValidationDto() {
    }

    /**
     * BlueprintValidator가 최소 검증을 통과시킨 뒤 파이프라인에 전달하는 내부 계약 모델.
     */
    public record ValidatedBlueprint(
        BlueprintOutputDto root,
        BlueprintOutputDto.AiBlueprint aiBlueprint,
        Map<CategoryType, Slot> slotsByCategory
    ) {
    }

    /**
     * coordination 내 단일 슬롯 원본 JSON을 감싼 내부 모델.
     */
    public record Slot(
        CategoryType slotKey,
        BlueprintOutputDto.CoordinationSlot raw
    ) {
    }
}
