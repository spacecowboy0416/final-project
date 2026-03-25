package com.finalproject.coordi.recommendation.service.productSearch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 쇼핑 검색 시스템을 호출하기 위한 아웃바운드 포트.
 */
public interface ShoppingPort {

    // 검색어와 제한 수를 받아 상품 후보 목록을 반환한다.
    List<SearchedProduct> search(ShoppingSearchQuery query);

    record ShoppingSearchQuery(
        @NotBlank String searchKeyword,
        @Positive int resultLimit
    ) {
        private static final int MIN_RESULT_LIMIT = 1;

        public ShoppingSearchQuery {
            // 검색어 공백 입력을 서비스 경계에서 정규화한다.
            searchKeyword = searchKeyword == null ? "" : searchKeyword.trim();
            // 최소 검색 개수 정책을 레코드에 캡슐화해 하드코딩 분산을 막는다.
            resultLimit = Math.max(resultLimit, MIN_RESULT_LIMIT);
        }
    }

    record SearchedProduct(
        String marketplaceProvider,
        String marketplaceProductId,
        String productName,
        String brandName,
        int salePrice,
        String productImageUrl,
        String productDetailUrl
    ) {
    }
}
