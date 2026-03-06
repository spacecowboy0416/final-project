package com.finalproject.coordi.recommendation.domain;

import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.SeasonType;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.TpoType;

/**
 * 추천 계산에 필요한 공통 컨텍스트를 보관한다.
 */
public record RequestContext(
    double currentTemp,
    TpoType tpoType,
    StyleMode styleMode,
    SeasonType season
) {
}
