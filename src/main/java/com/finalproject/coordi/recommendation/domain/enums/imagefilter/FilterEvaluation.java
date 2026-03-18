package com.finalproject.coordi.recommendation.domain.enums.imagefilter;

/**
 * 이미지 필터 판정 결과.
 */
public record FilterEvaluation(
    boolean passed,
    FilterReason reason,
    double personRatio,
    double garmentRatio
) {
}
