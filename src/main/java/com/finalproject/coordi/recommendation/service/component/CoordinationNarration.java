package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationResponseDto;
import com.finalproject.coordi.recommendation.dto.internal.AiBlueprintConstraintDto;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 4단계 스켈레톤: 검증된 코디와 슬롯별 최종 아이템을 자연어 중심 출력으로 변환한다.
 */
@Component
public class CoordinationNarration {
    public List<CoordinationOutputDto> toNaturalLanguage(
        AiBlueprintConstraintDto.Envelope validatedCoordination,
        List<CoordinationResponseDto.ItemResult> finalItems
    ) {
        throw new UnsupportedOperationException("TODO: 자연어 코디 출력 변환 구현");
    }
}
