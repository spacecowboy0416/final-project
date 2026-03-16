package com.finalproject.coordi.recommendation.service.apiport;

import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.service.component.FullPromptBuilder.FullPrompt;

/**
 * AI Blueprint 생성을 위한 아웃바운드 포트.
 */
public interface AiPort {
    /**
     * FullPrompt 입력을 기반으로 AI blueprint 출력 DTO를 생성한다.
     */
    BlueprintOutputDto generateBlueprint(FullPrompt fullPrompt);
}
