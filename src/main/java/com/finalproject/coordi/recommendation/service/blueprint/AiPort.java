package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;

/**
 * AI Blueprint 생성을 위한 아웃바운드 포트.
 */
public interface AiPort {
    RawBlueprintDto generateBlueprint(PayloadDto payload);
}



