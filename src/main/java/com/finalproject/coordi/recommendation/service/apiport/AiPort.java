package com.finalproject.coordi.recommendation.service.apiport;

import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.IntegratedPrompt;

/**
 * AI Blueprint 생성을 위한 아웃바운드 포트.
 */
public interface AiPort {
    /**
     * 입력 DTO를 기반으로 AI blueprint 출력 DTO를 생성한다.
     */
    BlueprintOutputDto generateBlueprint(IntegratedPrompt promptPayload, BlueprintInputDto.GeminiInputSchema inputDto);
}
