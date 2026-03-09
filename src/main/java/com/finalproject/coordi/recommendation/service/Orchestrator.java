package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.dto.api.CoordinationRequestDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationResponseDto;
import com.finalproject.coordi.recommendation.service.component.CoordinationNarration;
import com.finalproject.coordi.recommendation.service.component.CoordinationResponseAssembly;
import com.finalproject.coordi.recommendation.service.component.CoordinationRuleValidation;
import com.finalproject.coordi.recommendation.service.component.CoordinationValidator;
import com.finalproject.coordi.recommendation.service.component.ParallelAiAndPhotoIngestion;
import com.finalproject.coordi.recommendation.service.component.ParallelBestItemMapping;
import com.finalproject.coordi.recommendation.service.component.ParallelShoppingSearch;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 코디 추천 파이프라인 오케스트레이터 스켈레톤.
 *
 * 0. 입력
 * 1. AI 코디 생성 + 사진/메타데이터 DB 저장(병렬)
 * 2. 룰 엔진 검증
 * 3. 슬롯별 쇼핑 검색(병렬) + 최적 아이템 DB 매핑(병렬)
 * 4. 자연어 코디 출력 조립
 */
@Service
@Validated
@RequiredArgsConstructor
public class Orchestrator {
    private final ParallelAiAndPhotoIngestion parallelAiAndPhotoIngestion;
    private final CoordinationValidator coordinationValidator;
    private final CoordinationRuleValidation coordinationRuleValidation;
    private final ParallelShoppingSearch parallelShoppingSearch;
    private final ParallelBestItemMapping parallelBestItemMapping;
    private final CoordinationNarration coordinationNarration;
    private final CoordinationResponseAssembly coordinationResponseAssembly;

    @Transactional
    public CoordinationResponseDto coordinate(@Valid CoordinationRequestDto request) {
        // 1) AI 코디 생성 + 사진/메타데이터 저장 병렬 실행 결과
        var stage1 = parallelAiAndPhotoIngestion.execute(request);

        // 1-1) AI 출력 제약(스키마/필수 키/형식) 검증
        var constraintValidated = coordinationValidator.validateConstraints(stage1.aiRawBlueprint());

        // 2) 제약 검증 통과 결과를 비즈니스 룰 엔진으로 검증
        var validated = coordinationRuleValidation.validateBusinessRules(constraintValidated);

        // 3-1) 검증된 코디의 슬롯 기준 쇼핑 검색(병렬)
        var slotCandidates = parallelShoppingSearch.searchBySlots(validated);

        // 3-2) 후보 상품 중 코디 적합 아이템을 DB 기준으로 매핑(병렬)
        var finalItems = parallelBestItemMapping.mapBestItems(validated, slotCandidates);

        // 4) 검증된 코디 + 최종 아이템을 자연어 출력으로 변환
        var coordination = coordinationNarration.toNaturalLanguage(validated, finalItems);

        return coordinationResponseAssembly.assemble(stage1.aiRawBlueprint(), coordination, finalItems);
    }
}
