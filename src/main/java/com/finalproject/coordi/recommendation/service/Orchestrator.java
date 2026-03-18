package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.config.annotation.StageExecutionTimes;
import com.finalproject.coordi.recommendation.config.RecommendationPipelineProperties;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.PipelineMode;
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
import java.util.EnumMap;
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
    private final RecommendationPipelineProperties recommendationPipelineProperties;

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
        PipelineResult pipelineResult = runPipeline(request);
        return RecommendationDebugResponseDto.from(
            pipelineResult.rawBlueprint(),
            pipelineResult.coordinationOutput(),
            pipelineResult.slotSearchQueries(),
            pipelineResult.searchedProductsBySlot(),
            pipelineResult.filteredProductsBySlot(),
            stageExecutionTimes.snapshot()
        );
    }

    private PipelineResult runPipeline(@Valid UserRequestDto request) {
        PipelineMode pipelineMode = recommendationPipelineProperties.getMode();
        if (pipelineMode == PipelineMode.FAST_TOP1) {
            return runFastTop1Pipeline(request);
        }
        return runLegacyFullPipeline(request);
    }

    // 기존 파이프라인 단계를 모두 수행한다.
    private PipelineResult runLegacyFullPipeline(@Valid UserRequestDto request) {
        // 사용자 입력, 날씨, 프롬프트 조합하여 payload 생성
        var payload = payloadStage.build(request).payload();

        // ai 호출하여 blueprint 생성, 검증, 정규화(BlueprintStage 내에서 모두 수행)
        BlueprintStageResult blueprintResult = blueprintStage.generate(payload);

        // product search 단계에서 슬롯별 검색 쿼리 생성 및 검색을 수행한다.
        ProductSearchStageResult productSearchResult = productSearchStage.search(blueprintResult.normalizedBlueprint());

        // image filter 단계에서 검색 후보를 1차 이미지 품질 기준으로 필터링한다.
        var imageFilterResult = imageFilterStage.filter(productSearchResult.searchedProductsBySlot());

        // 이후 observation/scoring/final-output 고도화 이전 단계에서는 빈 매칭 결과를 사용한다.
        var coordinationResult = coordinationStage.match(blueprintResult.normalizedBlueprint(), productSearchResult);
        CoordinationOutputDto coordinationOutput = finalOutputBuilder.build(
            blueprintResult.normalizedBlueprint(),
            coordinationResult
        );

        return new PipelineResult(
            blueprintResult.rawBlueprint(),
            coordinationOutput,
            productSearchResult.slotSearchQueries(),
            productSearchResult.searchedProductsBySlot(),
            imageFilterResult.filteredProductsBySlot()
        );
    }

    // 축소 파이프라인: 검색 결과에서 슬롯별 TOP1만 사용하고 이미지 필터 단계는 건너뛴다.
    private PipelineResult runFastTop1Pipeline(@Valid UserRequestDto request) {
        var payload = payloadStage.build(request).payload();
        BlueprintStageResult blueprintResult = blueprintStage.generate(payload);
        ProductSearchStageResult productSearchResult = productSearchStage.search(blueprintResult.normalizedBlueprint());
        var coordinationResult = coordinationStage.match(blueprintResult.normalizedBlueprint(), productSearchResult);
        CoordinationOutputDto coordinationOutput = finalOutputBuilder.build(
            blueprintResult.normalizedBlueprint(),
            coordinationResult
        );

        return new PipelineResult(
            blueprintResult.rawBlueprint(),
            coordinationOutput,
            productSearchResult.slotSearchQueries(),
            productSearchResult.searchedProductsBySlot(),
            top1ProductsBySlot(productSearchResult.searchedProductsBySlot())
        );
    }

    private Map<CategoryType, List<SearchedProduct>> top1ProductsBySlot(
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
        EnumMap<CategoryType, List<SearchedProduct>> top1ProductsBySlot = new EnumMap<>(CategoryType.class);
        for (CategoryType categoryType : CategoryType.values()) {
            List<SearchedProduct> products = searchedProductsBySlot == null ? null : searchedProductsBySlot.get(categoryType);
            if (products == null || products.isEmpty()) {
                top1ProductsBySlot.put(categoryType, List.of());
                continue;
            }
            top1ProductsBySlot.put(categoryType, List.of(products.getFirst()));
        }
        return top1ProductsBySlot;
    }

    private record PipelineResult(
        RawBlueprintDto rawBlueprint,
        CoordinationOutputDto coordinationOutput,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries,
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot,
        Map<CategoryType, List<SearchedProduct>> filteredProductsBySlot
    ) {
    }
}
