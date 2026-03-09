package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.enums.CoordiContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.CoordiContextEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.BlueprintSource;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.RecommendationStatus;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationResponseDto;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 응답 조립 스켈레톤: 파이프라인 산출물을 API 응답 계약 DTO로 묶는다.
 */
@Component
public class CoordinationResponseAssembly {
    public CoordinationResponseDto assemble(
        JsonNode aiRawBlueprint,
        List<CoordinationOutputDto> coordination,
        List<CoordinationResponseDto.ItemResult> finalItems
    ) {
        return new CoordinationResponseDto(
            null,
            RecommendationStatus.PENDING,
            BlueprintSource.GEMINI,
            TpoType.CASUAL,
            StyleMode.COMFORTABLE,
            aiRawBlueprint,
            coordination,
            finalItems
        );
    }
}
