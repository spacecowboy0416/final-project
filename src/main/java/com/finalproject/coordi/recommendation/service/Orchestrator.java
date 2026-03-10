package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.service.component.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
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

    @Transactional
    // blueprint 추천 요청을 받아 전체 파이프라인을 순차 실행한다.
    public CoordinationOutputDto coordinate(@Valid BlueprintRequestDto request) {
        // 1. 날씨 조회
        var weather = weatherFetcher.fetch(request);

        // 2. 프롬프트 생성(natural text, image, location(kakao map), scheduleTime, weather)
        var prompt = promptBuilder.build(request, weather);

        // 3.1 AI API 호출하여 blueprint 생성
        var rawBlueprintJson = blueprintGenerator.generate(request, prompt);

        // 3.2 옷 사진 S3 업로드 및 메타데이터 저장
        var imageUrl = itemImageUploader.upload(request.imageData());
        itemMetadataRecorder.record(request, imageUrl);

        // 4. 내부 룰 엔진으로 생성된 blueprint 검증(스키마, 필수 슬롯, 적합성 등)
        var validatedBlueprint = blueprintValidator.validate(rawBlueprintJson);

        // 5. 검증된 blueprint slot별 item search_query 추출 및 ShoppingApi 검색
        var slotSearchQueries = itemSearchQueryExtractor.extract(validatedBlueprint);
        var slotCandidates = shoppingSearcher.searchAll(slotSearchQueries);

        // 6.  검색 결과 product schema upsert + 최적 아이템 매칭
        var matchedItemsBySlot = itemMatcher.matchAll(slotCandidates, validatedBlueprint);
        itemUpsertService.upsertAll(matchedItemsBySlot);

        // 7. 최종 응답 생성 및 노출
        return blueprintResponseBuilder.build(validatedBlueprint, matchedItemsBySlot);

        // 8. closet에 저장 및 시간, 위치, 태그 조정으로 재검색
        
    }
}


