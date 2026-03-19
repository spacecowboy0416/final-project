package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.service.Orchestrator.PipelineResult;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;

import java.util.List;
import java.util.Map;

/**
 * recommendation 테스트 페이지에서 최종 coordination과 slot별 searched product를 함께 확인하기 위한 디버그 응답 DTO.
 */
public record RecommendationDebugResponseDto(
    RawBlueprintDto rawBlueprint,
    String blueprintId,
    TpoType tpoType,
    StyleType styleType,
    String stylingRuleApplied,
    List<CoordinationItemOutputDto> coordination,
    Map<String, String> slotSearchQueries,
    Map<String, Long> stageTimings
) {
    public static RecommendationDebugResponseDto from(
        PipelineResult pipelineResult,
        Map<String, Long> stageTimings
    ) {
        if (pipelineResult == null || pipelineResult.coordinationOutput() == null) {
            return new RecommendationDebugResponseDto(
                null,
                null,
                null,
                null,
                null,
                List.of(),
                Map.of(),
                stageTimings == null ? Map.of() : stageTimings
            );
        }

        CoordinationOutputDto coordinationOutput = pipelineResult.coordinationOutput();
        return new RecommendationDebugResponseDto(
            pipelineResult.rawBlueprint(),
            coordinationOutput.blueprintId(),
            coordinationOutput.tpoType(),
            coordinationOutput.styleType(),
            coordinationOutput.stylingRuleApplied(),
            coordinationOutput.coordination(),
            toDebugSlotSearchQueries(pipelineResult.slotSearchQueries()),
            stageTimings == null ? Map.of() : stageTimings
        );
    }

    private static Map<String, String> toDebugSlotSearchQueries(
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        if (slotSearchQueries == null || slotSearchQueries.isEmpty()) {
            return Map.of();
        }
        return slotSearchQueries.entrySet().stream().collect(
            java.util.stream.Collectors.toMap(
                entry -> entry.getKey().getCode(),
                entry -> entry.getValue() == null ? "" : entry.getValue().searchKeyword()
            )
        );
    }
}
