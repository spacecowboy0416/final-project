package com.finalproject.coordi.recommendation.outbound;

import java.util.List;

/**
 * 쇼핑 검색 아웃바운드 포트.
 */
public interface ShoppingPort {
    /**
     * 검색어로 상품 후보 목록을 조회한다.
     */
    List<ShoppingProductCandidate> search(ShoppingSearchQuery query);
}
