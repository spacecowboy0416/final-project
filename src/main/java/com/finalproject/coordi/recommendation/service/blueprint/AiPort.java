package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;

/**
 * AI Blueprint 생성을 위한 아웃바운드 포트.
 */
public interface AiPort {
    /**
     * Payload 입력을 기반으로 AI blueprint 출력 DTO를 생성한다.
     */
    RawBlueprintDto generateBlueprint(PayloadDto payload);
}



