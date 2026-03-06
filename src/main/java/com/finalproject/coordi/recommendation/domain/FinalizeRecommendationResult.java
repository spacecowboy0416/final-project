package com.finalproject.coordi.recommendation.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.DraftSource;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.RecommendationStatus;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.TpoType;
import java.util.List;

/**
 * 추천 오케스트레이션 최종 결과를 보관한다.
 */
public record FinalizeRecommendationResult(
    Long recId,
    RecommendationStatus status,
    DraftSource draftSource,
    TpoType tpoType,
    StyleMode styleMode,
    ObjectNode aiBlueprint,
    List<SearchScoredItem> searchScoredItems
) {
}
