package com.finalproject.coordi.recommendation.domain;

import com.finalproject.coordi.recommendation.domain.type.SeasonType;
import com.finalproject.coordi.recommendation.domain.type.StyleMode;
import com.finalproject.coordi.recommendation.domain.type.TpoType;

/**
 * 추천 계산에 필요한 공통 컨텍스트를 보관한다.
 */
public record RecommendationContext(
    double currentTemp,
    TpoType tpoType,
    StyleMode styleMode,
    SeasonType season
) {
}
