package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.service.Orchestrator.PipelineResult;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
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
    Map<String, List<SearchedProductDebugDto>> searchedProductsBySlot,
    Map<String, List<SearchedProductDebugDto>> filteredProductsBySlot,
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
                Map.of(),
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
            toDebugSearchedProductsBySlot(pipelineResult.searchedProductsBySlot()),
            toDebugSearchedProductsBySlot(pipelineResult.filteredProductsBySlot()),
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
                entry -> entry.getKey().code(),
                entry -> entry.getValue() == null ? "" : entry.getValue().searchKeyword()
            )
        );
    }

    private static Map<String, List<SearchedProductDebugDto>> toDebugSearchedProductsBySlot(
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
        if (searchedProductsBySlot == null || searchedProductsBySlot.isEmpty()) {
            return Map.of();
        }
        return searchedProductsBySlot.entrySet().stream().collect(
            java.util.stream.Collectors.toMap(
                entry -> entry.getKey().code(),
                entry -> entry.getValue() == null
                    ? List.of()
                    : entry.getValue().stream().map(SearchedProductDebugDto::from).toList()
            )
        );
    }

    public record SearchedProductDebugDto(
        String marketplaceProvider,
        String marketplaceProductId,
        String productName,
        String brandName,
        int salePrice,
        String productImageUrl,
        String productDetailUrl
    ) {
        public static SearchedProductDebugDto from(SearchedProduct searchedProduct) {
            return new SearchedProductDebugDto(
                searchedProduct.marketplaceProvider(),
                searchedProduct.marketplaceProductId(),
                searchedProduct.productName(),
                searchedProduct.brandName(),
                searchedProduct.salePrice(),
                searchedProduct.productImageUrl(),
                searchedProduct.productDetailUrl()
            );
        }
    }
}
