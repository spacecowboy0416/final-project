package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.service.component.*;
import jakarta.validation.Valid;
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
    private final PromptBuilder promptBuilder;
    private final BlueprintGenerator blueprintGenerator;
    private final ItemImageUploader itemImageUploader;
    private final ItemMetadataRecorder itemMetadataRecorder;
    private final BlueprintValidator blueprintValidator;
    private final ItemSearchQueryExtractor itemSearchQueryExtractor;
    private final ShoppingSearcher shoppingSearcher;
    private final ItemMatcher itemMatcher;
    private final ItemUpserter itemUpsertService;
    private final BlueprintResponseBuilder blueprintResponseBuilder;

    //@Transactional 추후 구간 분리
    // blueprint 추천 요청을 받아 전체 파이프라인을 순차 실행한다.
    public CoordinationOutputDto coordinate(@Valid BlueprintInputDto request) {
        long pipelineStartedAt = System.nanoTime();

        // 1. 날씨 조회
        long weatherStartedAt = System.nanoTime();
        var weather = weatherFetcher.fetch(request);
        logStageDuration("weatherFetcher.fetch", weatherStartedAt);

        // 2. 프롬프트 생성(natural text, image, location(kakao map), scheduleTime, weather)
        long promptStartedAt = System.nanoTime();
        var systemUserPrompt = promptBuilder.build(request, weather);
        logStageDuration("promptBuilder.build", promptStartedAt);

        // 3.1 AI API 호출하여 blueprint 생성
        long blueprintStartedAt = System.nanoTime();
        var rawBlueprintJson = blueprintGenerator.generate(request, weather, systemUserPrompt);
        logStageDuration("blueprintGenerator.generate", blueprintStartedAt);

        // 3.2 옷 사진 S3 업로드 및 메타데이터 저장
        long imageUploadStartedAt = System.nanoTime();
        var imageUrl = itemImageUploader.upload(request.imageData());
        logStageDuration("itemImageUploader.upload", imageUploadStartedAt);

        long imageMetadataStartedAt = System.nanoTime();
        itemMetadataRecorder.record(request, imageUrl);
        logStageDuration("itemMetadataRecorder.record", imageMetadataStartedAt);

        // 4. 내부 룰 엔진으로 생성된 blueprint 검증(스키마, 필수 슬롯, 적합성 등)
        long validatorStartedAt = System.nanoTime();
        var validatedBlueprint = blueprintValidator.validate(rawBlueprintJson);
        logStageDuration("blueprintValidator.validate", validatorStartedAt);

        // 5. 검증된 blueprint slot별 item search_query 추출 및 ShoppingApi 검색
        long extractorStartedAt = System.nanoTime();
        var slotSearchQueries = itemSearchQueryExtractor.extract(validatedBlueprint);
        logStageDuration("itemSearchQueryExtractor.extract", extractorStartedAt);

        long shoppingStartedAt = System.nanoTime();
        var slotCandidates = shoppingSearcher.searchAll(slotSearchQueries);
        logStageDuration("shoppingSearcher.searchAll", shoppingStartedAt);

        // 6.  검색 결과 product schema upsert + 최적 아이템 매칭
        long matcherStartedAt = System.nanoTime();
        var matchedItemsBySlot = itemMatcher.matchAll(slotCandidates, validatedBlueprint);
        logStageDuration("itemMatcher.matchAll", matcherStartedAt);

        long upsertStartedAt = System.nanoTime();
        itemUpsertService.upsertAll(matchedItemsBySlot);
        logStageDuration("itemUpsertService.upsertAll", upsertStartedAt);

        // 7. 최종 응답 생성 및 노출
        long responseBuilderStartedAt = System.nanoTime();
        CoordinationOutputDto response = blueprintResponseBuilder.build(validatedBlueprint, matchedItemsBySlot, weather);
        logStageDuration("blueprintResponseBuilder.build", responseBuilderStartedAt);
        logStageDuration("recommendationPipeline.total", pipelineStartedAt);
        return response;
    }

    private void logStageDuration(String stageName, long startedAt) {
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("recommendation stage={} elapsedMs={}", stageName, elapsedMillis);
    }
}
