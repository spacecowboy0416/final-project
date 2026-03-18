package com.finalproject.coordi.recommendation.domain.enums.imagefilter;

/**
 * 이미지 필터 통과/탈락 사유를 표준화한다.
 */
public enum FilterReason {
    PASS_FILTER_DISABLED,
    PASS_THRESHOLD,
    REJECT_MISSING_IMAGE_URL,
    REJECT_ANALYSIS_UNAVAILABLE,
    REJECT_LOW_PERSON_RATIO,
    REJECT_LOW_GARMENT_RATIO
}
