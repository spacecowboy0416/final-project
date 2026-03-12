package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import java.util.List;

/**
 * Blueprint 기반 최종 coordination 출력 계약 DTO.
 */
public record CoordinationOutputDto(
    String blueprintId,
    TpoType tpoType,
    StyleType styleType,
    String stylingRuleApplied,
    List<BlueprintOutputDto> blueprint
) {
}
