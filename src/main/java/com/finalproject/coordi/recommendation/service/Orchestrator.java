package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.apiport.AiPort;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingSearchQuery;
import com.finalproject.coordi.recommendation.service.component.*;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor

/**
 * Blueprint 생성 파이프라인을 조율하고 최종 coordination 응답을 반환한다.
 */
public class Orchestrator {
    private final WeatherFetcher weatherFetcher;
    private final FullPromptBuilder fullPromptBuilder;
    private final AiPort aiPort;
    private final ItemImageUploader itemImageUploader;
    private final ItemMetadataRecorder itemMetadataRecorder;
    private final BlueprintValidator blueprintValidator;
    private final ItemSearchQueryExtractor itemSearchQueryExtractor;
    private final ShoppingSearcher shoppingSearcher;
    private final ItemMatcher itemMatcher;
    private final ItemUpserter itemUpsertService;
    private final CoordinationBuilder coordinationBuilder;

    //@Transactional 추후 구간 분리
    // blueprint 추천 요청을 받아 전체 파이프라인을 순차 실행한다.
    public CoordinationOutputDto coordinate(@Valid BlueprintInputDto request) {
        return runPipeline(request).coordinationOutput();
    }

    public RecommendationDebugResponseDto coordinateDebug(@Valid BlueprintInputDto request) {
        PipelineResult pipelineResult = runPipeline(request);
        return RecommendationDebugResponseDto.from(
            pipelineResult.rawBlueprint(),
            pipelineResult.coordinationOutput(),
            toDebugSlotSearchQueries(pipelineResult.slotSearchQueries()),
            toDebugSlotCandidates(pipelineResult.slotCandidates()),
            pipelineResult.stageTimings()
        );
    }

    private PipelineResult runPipeline(@Valid BlueprintInputDto request) {
        long pipelineStartedAt = System.nanoTime();
        Map<String, Long> stageTimings = new LinkedHashMap<>();

        // 1. 날씨 조회
        long weatherStartedAt = System.nanoTime();
        var weather = weatherFetcher.fetch(request);
        recordStageDuration("weatherFetcher.fetch", weatherStartedAt, stageTimings);

        // 2. 프롬프트 생성(natural text, image, location(kakao map), scheduleTime, weather)
        long promptStartedAt = System.nanoTime();
        var fullPrompt = fullPromptBuilder.build(request, weather);
        recordStageDuration("fullPromptBuilder.build", promptStartedAt, stageTimings);

        // 3.1 AI API 호출하여 blueprint 생성
        long blueprintStartedAt = System.nanoTime();
        var rawBlueprintJson = aiPort.generateBlueprint(fullPrompt);
        recordStageDuration("aiPort.generateBlueprint", blueprintStartedAt, stageTimings);

        // 3.2 옷 사진 S3 업로드 및 메타데이터 저장
        long imageUploadStartedAt = System.nanoTime();
        var imageUrl = itemImageUploader.upload(request.imageData());
        recordStageDuration("itemImageUploader.upload", imageUploadStartedAt, stageTimings);

        long imageMetadataStartedAt = System.nanoTime();
        itemMetadataRecorder.record(request, imageUrl);
        recordStageDuration("itemMetadataRecorder.record", imageMetadataStartedAt, stageTimings);

        // 4. 내부 룰 엔진으로 생성된 blueprint 검증(스키마, 필수 슬롯, 적합성 등)
        long validatorStartedAt = System.nanoTime();
        var validatedBlueprint = blueprintValidator.validate(rawBlueprintJson);
        recordStageDuration("blueprintValidator.validate", validatorStartedAt, stageTimings);

        // 5. 검증된 blueprint slot별 item search_query 추출 및 ShoppingApi 검색
        long extractorStartedAt = System.nanoTime();
        var slotSearchQueries = itemSearchQueryExtractor.extract(validatedBlueprint, request);
        recordStageDuration("itemSearchQueryExtractor.extract", extractorStartedAt, stageTimings);

        long shoppingStartedAt = System.nanoTime();
        var slotCandidates = shoppingSearcher.searchAll(slotSearchQueries);
        recordStageDuration("shoppingSearcher.searchAll", shoppingStartedAt, stageTimings);

        // 6.  검색 결과 product schema upsert + 최적 아이템 매칭
        long matcherStartedAt = System.nanoTime();
        var matchedItemsBySlot = itemMatcher.matchAll(slotCandidates, validatedBlueprint);
        recordStageDuration("itemMatcher.matchAll", matcherStartedAt, stageTimings);

        long upsertStartedAt = System.nanoTime();
        itemUpsertService.upsertAll(matchedItemsBySlot);
        recordStageDuration("itemUpsertService.upsertAll", upsertStartedAt, stageTimings);

        // 7. 최종 응답 생성 및 노출
        long responseBuilderStartedAt = System.nanoTime();
        CoordinationOutputDto response = coordinationBuilder.build(validatedBlueprint, matchedItemsBySlot, weather);
        recordStageDuration("coordinationBuilder.build", responseBuilderStartedAt, stageTimings);
        recordStageDuration("recommendationPipeline.total", pipelineStartedAt, stageTimings);
        return new PipelineResult(
            rawBlueprintJson,
            response,
            slotSearchQueries,
            slotCandidates,
            stageTimings
        );
    }

    private Map<String, ShoppingSearchQuery> toDebugSlotSearchQueries(
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        if (slotSearchQueries == null || slotSearchQueries.isEmpty()) {
            return Map.of();
        }
        return slotSearchQueries.entrySet().stream().collect(
            java.util.stream.Collectors.toMap(
                entry -> entry.getKey().code(),
                Map.Entry::getValue
            )
        );
    }

    private Map<String, List<ShoppingProductCandidate>> toDebugSlotCandidates(
        Map<CategoryType, List<ShoppingProductCandidate>> slotCandidates
    ) {
        if (slotCandidates == null || slotCandidates.isEmpty()) {
            return Map.of();
        }
        return slotCandidates.entrySet().stream().collect(
            java.util.stream.Collectors.toMap(
                entry -> entry.getKey().code(),
                Map.Entry::getValue
            )
        );
    }

    private void recordStageDuration(String stageName, long startedAt, Map<String, Long> stageTimings) {
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        stageTimings.put(stageName, elapsedMillis);
        log.info("recommendation stage={} elapsedMs={}", stageName, elapsedMillis);
    }

    private record PipelineResult(
        com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto rawBlueprint,
        CoordinationOutputDto coordinationOutput,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries,
        Map<CategoryType, List<ShoppingProductCandidate>> slotCandidates,
        Map<String, Long> stageTimings
    ) {
    }
}
