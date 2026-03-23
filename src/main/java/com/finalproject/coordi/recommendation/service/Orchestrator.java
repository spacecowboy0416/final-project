package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.config.annotation.StageExecutionTimes;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.service.blueprint.BlueprintStage;
import com.finalproject.coordi.recommendation.service.blueprint.BlueprintStage.BlueprintStageResult;
import com.finalproject.coordi.recommendation.service.finaloutput.FinalOutputStage;
import com.finalproject.coordi.recommendation.service.finaloutput.FinalOutputStage.DebugInput;
import com.finalproject.coordi.recommendation.service.finaloutput.FinalOutputStage.PersistInput;
import com.finalproject.coordi.recommendation.service.payload.PayloadStage;
import com.finalproject.coordi.recommendation.service.productSearch.ProductSearchStage;
import com.finalproject.coordi.recommendation.service.productSearch.ProductSearchStage.ProductSearchStageResult;
import com.finalproject.coordi.recommendation.service.productSearch.SlotSearchQueries;
import jakarta.validation.Valid;
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
    private final FinalOutputStage finalOutputStage;
    private final StageExecutionTimes stageExecutionTimes;

    /**
     * recommendation 표준 응답만 필요한 일반 API 진입점이다.
     */
    public CoordinationOutputDto coordinate(@Valid UserRequestDto request, Long userId) {
        PipelineResult pipelineResult = runPipeline(request, userId, OutputMode.PERSIST);
        return pipelineResult.coordinationOutput();
    }

    /**
     * 디버그 화면용으로 중간 산출물과 stage timing까지 함께 반환하는 진입점이다.
     */
    public RecommendationDebugResponseDto coordinateDebug(@Valid UserRequestDto request) {
        PipelineResult pipelineResult = runPipeline(request, null, OutputMode.DEBUG);
        return RecommendationDebugResponseDto.from(pipelineResult, stageExecutionTimes.snapshot());
    }

    private PipelineResult runPipeline(
        UserRequestDto request,
        Long userId,
        OutputMode outputMode
    ) {
        // 사용자 입력, 날씨, 프롬프트 조합하여 payload 생성
        var payload = payloadStage.build(request);

        // AI 호출하여 blueprint 생성, 검증, 정규화
        BlueprintStageResult blueprintResult = blueprintStage.generate(payload);

        // 슬롯별 검색 쿼리 생성 및 상품 검색
        ProductSearchStageResult productSearchResult = productSearchStage.search(
            blueprintResult.normalizedBlueprint(),
            request.brandEnabled()
        );

        // 최종 출력 생성(일반 API는 저장, 디버그 API는 조회 전용)
        CoordinationOutputDto coordinationOutput = switch (outputMode) {
            case PERSIST -> finalOutputStage.buildPersist(new PersistInput(
                request,
                userId,
                blueprintResult.normalizedBlueprint(),
                productSearchResult
            ));
            case DEBUG -> finalOutputStage.buildDebug(new DebugInput(
                blueprintResult.normalizedBlueprint(),
                productSearchResult
            ));
        };

        return new PipelineResult(
            blueprintResult.rawBlueprint(),
            productSearchResult.slotSearchQueries(),
            coordinationOutput
        );
    }

    public static record PipelineResult(
        RawBlueprintDto rawBlueprint,
        SlotSearchQueries slotSearchQueries,
        CoordinationOutputDto coordinationOutput
    ) {
    }

    private enum OutputMode {
        PERSIST,
        DEBUG
    }
}
