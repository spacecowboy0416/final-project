package com.finalproject.coordi.recommendation.outbound;

/**
 * 쇼핑 검색 요청 파라미터를 보관한다.
 */
public record ShoppingSearchQuery(
    String query,
    int display
) {
}
