package com.finalproject.coordi.recommendation.dto.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import java.util.List;

/**
 * Blueprint 기반 최종 coordination 출력 계약 DTO.
 */
public record CoordinationOutputDto(
    String blueprintId,
    String status,
    String blueprintSource,
    TpoType tpoType,
    StyleType styleType,
    JsonNode aiBlueprint,
    List<BlueprintOutputDto> blueprint
) {
}
