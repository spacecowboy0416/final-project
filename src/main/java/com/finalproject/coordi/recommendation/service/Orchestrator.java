package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.config.annotation.StageExecutionTimes;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.blueprint.BlueprintDebugStage;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.blueprint.BlueprintStage;
import com.finalproject.coordi.recommendation.service.coordination.CoordinationStage;
import com.finalproject.coordi.recommendation.service.coordination.ItemMatcher;
import com.finalproject.coordi.recommendation.service.finaloutput.FinalOutputBuilder;
import com.finalproject.coordi.recommendation.service.payload.PayloadStage;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.ShoppingSearchQuery;
import com.finalproject.coordi.recommendation.service.product.ProductStage;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor

/**
 * Blueprint 생성 파이프라인을 조율하고 최종 coordination 응답을 반환한다.
 */
public class Orchestrator {
    private final PayloadStage payloadStage;
    private final BlueprintStage blueprintStage;
    private final BlueprintDebugStage blueprintDebugStage;
    private final ProductStage productStage;
    private final CoordinationStage coordinationStage;
    private final FinalOutputBuilder finalOutputBuilder;
    private final StageExecutionTimes stageExecutionTimes;

    /**
     * recommendation 표준 응답만 필요한 일반 API 진입점이다.
     */
    public CoordinationOutputDto coordinate(@Valid UserRequestDto request) {
        return runPipeline(request).coordinationOutput();
    }

    /**
     * 디버그 화면용으로 중간 산출물과 stage timing까지 함께 반환하는 진입점이다.
     */
    public RecommendationDebugResponseDto coordinateDebug(@Valid UserRequestDto request) {
        DebugPipelineResult pipelineResult = runDebugPipeline(request);
        return RecommendationDebugResponseDto.from(
            pipelineResult.rawBlueprint(),
            pipelineResult.coordinationOutput(),
            pipelineResult.slotSearchQueries(),
            pipelineResult.searchedProductsBySlot(),
            stageExecutionTimes.snapshot()
        );
    }

    private PipelineResult runPipeline(@Valid UserRequestDto request) {

        //사용자 입력, 날씨, 프롬프트 조합하여 payload 생성
        var payload = payloadStage.build(request).payload();

        //ai 호출하여 blueprint 생성, 검증, 정규화(BlueprintStage 내에서 모두 수행)
        NormalizedBlueprintDto normalizedBlueprintDto = blueprintStage.generateNormalizedBlueprint(payload);

        // product 단계에서 쿼리 추출/검색/이미지 다운로드/DB upsert/S3 upsert를 모두 수행한다.
        productStage.process(normalizedBlueprintDto);

        // NormalizedBlueprint와 가장 적절한 product 매칭을 수행한다.
        CoordinationStage.CoordinationResult coordinationResult =
            coordinationStage.coordinate(normalizedBlueprintDto, Map.of());
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot =
            coordinationResult.matchedItemsBySlot();

        // slot별 matched item 가져와 최종 응답 형태로 변환한다
        CoordinationOutputDto response = finalOutputBuilder.build(normalizedBlueprintDto, matchedItemsBySlot);
        return new PipelineResult(
            normalizedBlueprintDto,
            response,
            Map.of(),
            Map.of()
        );
    }

    private DebugPipelineResult runDebugPipeline(@Valid UserRequestDto request) {
        PayloadDto payload = payloadStage.build(request).payload();

        BlueprintDebugStage.DebugBlueprintResult debugBlueprintResult =
            blueprintDebugStage.generateValidatedDebugBlueprint(payload);
        RawBlueprintDto rawBlueprint = debugBlueprintResult.rawBlueprint();
        NormalizedBlueprintDto normalizedBlueprintDto = debugBlueprintResult.validatedBlueprint();

        productStage.process(normalizedBlueprintDto);

        CoordinationStage.CoordinationResult coordinationResult =
            coordinationStage.coordinate(normalizedBlueprintDto, Map.of());
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot =
            coordinationResult.matchedItemsBySlot();

        CoordinationOutputDto response = finalOutputBuilder.build(normalizedBlueprintDto, matchedItemsBySlot);
        return new DebugPipelineResult(
            rawBlueprint,
            response,
            Map.of(),
            Map.of()
        );
    }

    private record PipelineResult(
        NormalizedBlueprintDto normalizedBlueprint,
        CoordinationOutputDto coordinationOutput,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries,
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
    }

    private record DebugPipelineResult(
        RawBlueprintDto rawBlueprint,
        CoordinationOutputDto coordinationOutput,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries,
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
    }

}
