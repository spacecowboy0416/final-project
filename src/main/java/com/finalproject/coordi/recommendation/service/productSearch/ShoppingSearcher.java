package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;
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
    // 디버그 검색 엔드포인트에서 사용할 기본 검색 개수다.
    private static final int DEBUG_RESULT_LIMIT = 1;

    private final ShoppingPort shoppingPort;

    public List<SearchedProduct> search(String searchKeyword) {
        ShoppingSearchQuery query = new ShoppingSearchQuery(searchKeyword, DEBUG_RESULT_LIMIT);
        return shoppingPort.search(query);
    }

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
