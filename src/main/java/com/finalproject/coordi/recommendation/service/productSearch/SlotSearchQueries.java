package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 슬롯별 검색 쿼리를 캡슐화해 복잡한 제네릭 타입 노출을 줄인다.
 */
public record SlotSearchQueries(
    Map<CategoryType, ShoppingSearchQuery> values
) {
    public SlotSearchQueries {
        Objects.requireNonNull(values, "values must not be null.");
        if (values.isEmpty()) {
            values = Map.of();
        } else {
            EnumMap<CategoryType, ShoppingSearchQuery> copied = new EnumMap<>(CategoryType.class);
            values.forEach((categoryType, searchQuery) -> {
                if (categoryType == null) {
                    throw new IllegalArgumentException("slot key must not be null.");
                }
                if (searchQuery == null) {
                    throw new IllegalArgumentException("search query must not be null.");
                }
                copied.put(categoryType, searchQuery);
            });
            values = Collections.unmodifiableMap(copied);
        }
    }

    public static SlotSearchQueries empty() {
        return new SlotSearchQueries(Map.of());
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public ShoppingSearchQuery queryOf(CategoryType categoryType) {
        Objects.requireNonNull(categoryType, "categoryType must not be null.");
        return values.get(categoryType);
    }

    public String searchKeywordOf(CategoryType categoryType) {
        ShoppingSearchQuery query = queryOf(categoryType);
        return query == null ? "" : query.searchKeyword();
    }

    public void forEach(BiConsumer<CategoryType, ShoppingSearchQuery> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null.");
        values.forEach(consumer);
    }
}
