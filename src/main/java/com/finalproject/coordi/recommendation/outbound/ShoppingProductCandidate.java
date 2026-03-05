package com.finalproject.coordi.recommendation.outbound;

/**
 * 쇼핑 API 검색 결과 아이템을 보관한다.
 */
public record ShoppingProductCandidate(
    String source,
    String externalId,
    String name,
    String brand,
    int price,
    String imageUrl,
    String link
) {
}
