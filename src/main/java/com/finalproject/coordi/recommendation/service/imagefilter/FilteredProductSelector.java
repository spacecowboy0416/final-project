package com.finalproject.coordi.recommendation.service.imagefilter;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 슬롯별 검색 후보에서 필터 통과 상품만 남긴다.
 */
@Component
@RequiredArgsConstructor
public class FilteredProductSelector {
    private final ImageFilterEvaluator imageFilterEvaluator;

    public Map<CategoryType, List<SearchedProduct>> select(
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
        Map<CategoryType, List<SearchedProduct>> filteredProductsBySlot = new EnumMap<>(CategoryType.class);
        if (searchedProductsBySlot == null || searchedProductsBySlot.isEmpty()) {
            return filteredProductsBySlot;
        }

        searchedProductsBySlot.forEach((slotKey, searchedProducts) -> {
            List<SearchedProduct> filteredProducts = new ArrayList<>();
            if (searchedProducts != null) {
                for (SearchedProduct searchedProduct : searchedProducts) {
                    if (imageFilterEvaluator.evaluate(searchedProduct).passed()) {
                        filteredProducts.add(searchedProduct);
                    }
                }
            }
            filteredProductsBySlot.put(slotKey, filteredProducts);
        });
        return filteredProductsBySlot;
    }
}
