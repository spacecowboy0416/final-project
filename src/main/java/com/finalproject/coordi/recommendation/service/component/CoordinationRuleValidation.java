package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.internal.AiBlueprintConstraintDto;
import org.springframework.stereotype.Component;

/**
 * 2단계 스켈레톤: 제약 검증을 통과한 코디를 비즈니스 룰 엔진으로 검증한다.
 */
@Component
public class CoordinationRuleValidation {
    public AiBlueprintConstraintDto.Envelope validateBusinessRules(
        AiBlueprintConstraintDto.Envelope constraintValidatedBlueprint
    ) {
        throw new UnsupportedOperationException("TODO: 비즈니스 룰 엔진 검증 구현");
    }
}
