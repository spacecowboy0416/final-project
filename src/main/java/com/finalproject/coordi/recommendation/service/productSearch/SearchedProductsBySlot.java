package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 슬롯별 검색 상품 컬렉션을 캡슐화해 도메인 규칙 메서드를 제공한다.
 */
public record SearchedProductsBySlot(
    Map<CategoryType, List<SearchedProduct>> values
) {
    public SearchedProductsBySlot {
        Objects.requireNonNull(values, "values must not be null.");
        if (values.isEmpty()) {
            values = Map.of();
        } else {
            EnumMap<CategoryType, List<SearchedProduct>> copied = new EnumMap<>(CategoryType.class);
            values.forEach((categoryType, products) -> {
                if (categoryType == null) {
                    throw new IllegalArgumentException("slot key must not be null.");
                }
                if (products == null) {
                    throw new IllegalArgumentException("searched products must not be null.");
                }
                copied.put(categoryType, Collections.unmodifiableList(new ArrayList<>(products)));
            });
            values = Collections.unmodifiableMap(copied);
        }
    }

    public static SearchedProductsBySlot empty() {
        return new SearchedProductsBySlot(Map.of());
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public List<SearchedProduct> productsOf(CategoryType categoryType) {
        Objects.requireNonNull(categoryType, "categoryType must not be null.");
        List<SearchedProduct> products = values.get(categoryType);
        return products == null ? List.of() : products;
    }

    public SearchedProduct top1(CategoryType categoryType) {
        List<SearchedProduct> products = productsOf(categoryType);
        if (products.isEmpty()) {
            return null;
        }
        return products.getFirst();
    }

    public void forEach(BiConsumer<CategoryType, List<SearchedProduct>> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null.");
        values.forEach(consumer);
    }
}
