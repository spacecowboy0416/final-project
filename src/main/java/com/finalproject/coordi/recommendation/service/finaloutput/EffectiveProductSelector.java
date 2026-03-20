package com.finalproject.coordi.recommendation.service.finaloutput;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.List;
import java.util.Map;

/**
 * 슬롯별 검색 결과 맵에서 TOP1 상품 추출 규칙을 공통으로 제공한다.
 */
public final class EffectiveProductSelector {
    private EffectiveProductSelector() {
    }

    public static SearchedProduct extractTop1Product(
        CategoryType categoryType,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        if (effectiveProducts == null) {
            return null;
        }
        List<SearchedProduct> products = effectiveProducts.get(categoryType);
        if (products == null || products.isEmpty()) {
            return null;
        }
        return products.getFirst();
    }
}
