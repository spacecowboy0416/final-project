package com.finalproject.coordi.recommendation.service.finaloutput;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.productSearch.ProductSearchStage.ProductSearchStageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Final Output 단계를 한곳에 묶어 최종 응답 생성과 저장 정책을 수행한다.
 */
@Component
@Validated
@RequiredArgsConstructor
public class FinalOutputStage {
    private final FinalOutputBuilder finalOutputBuilder;

    @LogStage("finalOutput.persist")
    public CoordinationOutputDto buildPersist(
        @Valid @NotNull PersistInput input
    ) {
        return finalOutputBuilder.buildAndPersist(
            input.request(),
            input.userId(),
            input.normalizedBlueprint(),
            input.productSearchResult().searchedProductsBySlot(),
            input.productSearchResult().slotSearchQueries()
        );
    }

    @LogStage("finalOutput.debug")
    public CoordinationOutputDto buildDebug(
        @Valid @NotNull DebugInput input
    ) {
        return finalOutputBuilder.build(
            input.normalizedBlueprint(),
            input.productSearchResult().searchedProductsBySlot()
        );
    }

    // 저장 경로 입력 계약
    public record PersistInput(
        @Valid @NotNull UserRequestDto request,
        Long userId,
        @NotNull NormalizedBlueprintDto normalizedBlueprint,
        @NotNull ProductSearchStageResult productSearchResult
    ) {
    }

    // 디버그 경로 입력 계약
    public record DebugInput(
        @NotNull NormalizedBlueprintDto normalizedBlueprint,
        @NotNull ProductSearchStageResult productSearchResult
    ) {
    }
}
