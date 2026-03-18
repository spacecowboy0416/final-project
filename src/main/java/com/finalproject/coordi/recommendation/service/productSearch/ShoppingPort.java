package com.finalproject.coordi.recommendation.service.productSearch;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 쇼핑 검색 시스템을 호출하기 위한 아웃바운드 포트.
 */
public interface ShoppingPort {

    // 검색어와 제한 수를 받아 상품 후보 목록을 반환한다.
    List<SearchedProduct> search(ShoppingSearchQuery query);

    record ShoppingSearchQuery(
        @NotBlank String searchKeyword,
        int resultLimit
    ) {
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

