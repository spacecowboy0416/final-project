package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 슬롯별 쇼핑 검색을 수행하고 결과를 수집한다.
 */
@Component
@RequiredArgsConstructor
public class ShoppingSearcher {
    private final ShoppingPort shoppingPort;

    public SearchedProductsBySlot searchBySlot(
        SlotSearchQueries slotSearchQueries
    ) {
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot = new EnumMap<>(CategoryType.class);
        slotSearchQueries.forEach((slotKey, searchQuery) -> {
            List<SearchedProduct> searchedProducts = shoppingPort.search(searchQuery);
            searchedProductsBySlot.put(slotKey, searchedProducts);
        });
        return new SearchedProductsBySlot(searchedProductsBySlot);
    }
}
