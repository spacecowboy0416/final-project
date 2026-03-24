package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.service.Orchestrator.PipelineResult;
import com.finalproject.coordi.recommendation.service.productSearch.SlotSearchQueries;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * recommendation 테스트 페이지에서 blueprint, 슬롯별 검색 쿼리, 최종 coordination,
 * stage timing을 함께 확인하기 위한 디버그 응답 DTO.
 */
public record RecommendationDebugResponseDto(
    RawBlueprintDto rawBlueprint,
    String blueprintId,
    TpoType tpoType,
    StyleType styleType,
    CategoryType anchorSlot,
    String aiExplanation,
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
            pipelineResult.rawBlueprint() == null || pipelineResult.rawBlueprint().aiBlueprint() == null
                ? null
                : pipelineResult.rawBlueprint().aiBlueprint().anchorSlot(),
            coordinationOutput.aiExplanation(),
            coordinationOutput.coordination(),
            toDebugSlotSearchQueries(pipelineResult.slotSearchQueries()),
            stageTimings == null ? Map.of() : stageTimings
        );
    }

    private static Map<String, String> toDebugSlotSearchQueries(
        SlotSearchQueries slotSearchQueries
    ) {
        if (slotSearchQueries == null || slotSearchQueries.isEmpty()) {
            return Map.of();
        }
        Map<String, String> debugQueries = new LinkedHashMap<>();
        slotSearchQueries.forEach((categoryType, query) -> {
            if (categoryType == null) {
                return;
            }
            debugQueries.put(categoryType.getCode(), query == null ? "" : query.searchKeyword());
        });
        return debugQueries;
    }
}
