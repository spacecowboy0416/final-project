package com.finalproject.coordi.recommendation.service.finaloutput;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.SearchedProductsBySlot;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;

/**
 * 슬롯별 검색 결과 맵에서 TOP1 상품 추출 규칙을 공통으로 제공한다.
 */
public final class EffectiveProductSelector {
    private EffectiveProductSelector() {
    }

    public static SearchedProduct extractTop1Product(
        CategoryType categoryType,
        SearchedProductsBySlot effectiveProducts
    ) {
        return effectiveProducts.top1(categoryType);
    }
}
