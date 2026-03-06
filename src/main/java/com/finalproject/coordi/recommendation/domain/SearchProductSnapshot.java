package com.finalproject.coordi.recommendation.domain;

/**
 * 추천 결과에 포함할 상품 미리보기 정보를 보관한다.
 */
public record SearchProductSnapshot(
    String name,
    int price,
    String imageUrl,
    String link
) {
}
