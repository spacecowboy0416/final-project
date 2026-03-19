package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.config.annotation.StageExecutionTimes;
import com.finalproject.coordi.recommendation.config.RecommendationProperties;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.blueprint.BlueprintStage;
import com.finalproject.coordi.recommendation.service.blueprint.BlueprintStage.BlueprintStageResult;
import com.finalproject.coordi.recommendation.service.coordination.CoordinationStage;
import com.finalproject.coordi.recommendation.service.finaloutput.FinalOutputBuilder;
import com.finalproject.coordi.recommendation.service.imagefilter.ImageFilterStage;
import com.finalproject.coordi.recommendation.service.payload.PayloadStage;
import com.finalproject.coordi.recommendation.service.productSearch.ProductSearchStage;
import com.finalproject.coordi.recommendation.service.productSearch.ProductSearchStage.ProductSearchStageResult;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;

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
    private final ProductSearchStage productSearchStage;
    private final ImageFilterStage imageFilterStage;
    private final CoordinationStage coordinationStage;
    private final FinalOutputBuilder finalOutputBuilder;
    private final StageExecutionTimes stageExecutionTimes;
    private final RecommendationProperties recommendationProperties;

    /**
     * recommendation 표준 응답만 필요한 일반 API 진입점이다.
     */
    public CoordinationOutputDto coordinate(@Valid UserRequestDto request, Long userId) {
        PipelineArtifacts pipelineArtifacts = runPipelineStages(request);
        return finalOutputBuilder.buildAndPersist(
            request,
            userId,
            pipelineArtifacts.payload(),
            pipelineArtifacts.normalizedBlueprint(),
            pipelineArtifacts.effectiveProducts(),
            pipelineArtifacts.slotSearchQueries()
        );
    }

    /**
     * 디버그 화면용으로 중간 산출물과 stage timing까지 함께 반환하는 진입점이다.
     */
    public RecommendationDebugResponseDto coordinateDebug(@Valid UserRequestDto request) {
        return coordinateDebug(request, null, false);
    }

    /**
     * 디버그 화면 요청에서 필요 시 저장까지 함께 수행한다.
     */
    public RecommendationDebugResponseDto coordinateDebug(@Valid UserRequestDto request, Long userId, boolean persist) {
        PipelineArtifacts pipelineArtifacts = runPipelineStages(request);
        CoordinationOutputDto coordinationOutput = persist
            ? finalOutputBuilder.buildAndPersist(
                request,
                userId,
                pipelineArtifacts.payload(),
                pipelineArtifacts.normalizedBlueprint(),
                pipelineArtifacts.effectiveProducts(),
                pipelineArtifacts.slotSearchQueries()
            )
            : finalOutputBuilder.build(
                pipelineArtifacts.normalizedBlueprint(),
                pipelineArtifacts.effectiveProducts()
            );
        PipelineResult pipelineResult = new PipelineResult(
            pipelineArtifacts.rawBlueprint(),
            coordinationOutput,
            pipelineArtifacts.slotSearchQueries()
        );
        return RecommendationDebugResponseDto.from(pipelineResult, stageExecutionTimes.snapshot());
    }

    private PipelineArtifacts runPipelineStages(@Valid UserRequestDto request) {
        // 사용자 입력, 날씨, 프롬프트 조합하여 payload 생성
        PayloadDto payload = payloadStage.build(request).payload();

        // AI 호출하여 blueprint 생성, 검증, 정규화
        BlueprintStageResult blueprintResult = blueprintStage.generate(payload);

        // 슬롯별 검색 쿼리 생성 및 상품 검색
        ProductSearchStageResult productSearchResult = productSearchStage.search(
            blueprintResult.normalizedBlueprint(),
            request.brandEnabled()
        );

        // FAST_TOP1: 검색 결과를 바로 최종 출력에 사용한다.
        // LEGACY_FULL: 기존 filter/coordination 경로를 유지한다.
        Map<CategoryType, List<SearchedProduct>> effectiveProducts;
        if (recommendationProperties.isFastTop1Enabled()) {
            effectiveProducts = productSearchResult.searchedProductsBySlot();
        } else {
            effectiveProducts = imageFilterStage.filter(productSearchResult.searchedProductsBySlot()).filteredProductsBySlot();
            coordinationStage.match(blueprintResult.normalizedBlueprint(), productSearchResult);
        }

        return new PipelineArtifacts(
            payload,
            blueprintResult.rawBlueprint(),
            blueprintResult.normalizedBlueprint(),
            productSearchResult.slotSearchQueries(),
            effectiveProducts
        );
    }

    private record PipelineArtifacts(
        PayloadDto payload,
        RawBlueprintDto rawBlueprint,
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
    }

    public static record PipelineResult(
        RawBlueprintDto rawBlueprint,
        CoordinationOutputDto coordinationOutput,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
    }
}
