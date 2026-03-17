package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingSearchQuery;
import java.util.List;
import java.util.Map;

/**
 * recommendation 테스트 페이지에서 최종 coordination과 slot별 shopping 후보를 함께 확인하기 위한 디버그 응답 DTO.
 */
public record RecommendationDebugResponseDto(
    BlueprintOutputDto geminiBlueprint,
    String blueprintId,
    TpoType tpoType,
    StyleType styleType,
    String stylingRuleApplied,
    List<CoordinationItemOutputDto> coordination,
    Map<String, String> slotSearchQueries,
    Map<String, List<ShoppingCandidateDebugDto>> slotCandidates,
    Map<String, Long> stageTimings
) {
    public static RecommendationDebugResponseDto from(
        BlueprintOutputDto geminiBlueprint,
        CoordinationOutputDto coordinationOutput,
        Map<String, ShoppingSearchQuery> slotSearchQueries,
        Map<String, List<ShoppingProductCandidate>> slotCandidates,
        Map<String, Long> stageTimings
    ) {
        return new RecommendationDebugResponseDto(
            geminiBlueprint,
            coordinationOutput.blueprintId(),
            coordinationOutput.tpoType(),
            coordinationOutput.styleType(),
            coordinationOutput.stylingRuleApplied(),
            coordinationOutput.coordination(),
            slotSearchQueries == null
                ? Map.of()
                : slotSearchQueries.entrySet().stream().collect(
                    java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() == null ? "" : entry.getValue().searchKeyword()
                    )
                ),
            slotCandidates == null
                ? Map.of()
                : slotCandidates.entrySet().stream().collect(
                    java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() == null
                            ? List.of()
                            : entry.getValue().stream().map(ShoppingCandidateDebugDto::from).toList()
                    )
                ),
            stageTimings == null ? Map.of() : stageTimings
        );
    }

    public record ShoppingCandidateDebugDto(
        String marketplaceProvider,
        String marketplaceProductId,
        String productName,
        String brandName,
        int salePrice,
        String productImageUrl,
        String productDetailUrl
    ) {
        public static ShoppingCandidateDebugDto from(ShoppingProductCandidate candidate) {
            return new ShoppingCandidateDebugDto(
                candidate.marketplaceProvider(),
                candidate.marketplaceProductId(),
                candidate.productName(),
                candidate.brandName(),
                candidate.salePrice(),
                candidate.productImageUrl(),
                candidate.productDetailUrl()
            );
        }
    }
}
