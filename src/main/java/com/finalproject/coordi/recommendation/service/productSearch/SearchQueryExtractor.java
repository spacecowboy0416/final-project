package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.infra.navershopping.NaverShoppingProperties;
import com.finalproject.coordi.recommendation.infra.navershopping.policy.NaverShoppingQueryPolicy;
import com.finalproject.coordi.recommendation.infra.navershopping.policy.NaverShoppingQueryPolicy.QueryTokenType;
import com.finalproject.coordi.recommendation.infra.navershopping.policy.NaverShoppingQueryPolicy.SearchQueryContext;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;

import java.util.LinkedHashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchQueryExtractor {
    private final NaverShoppingProperties shoppingProperties;
    private final NaverShoppingQueryPolicy queryPolicy;

    public Map<CategoryType, ShoppingSearchQuery> extract(
        NormalizedBlueprintDto normalizedBlueprint,
        Boolean brandEnabled
    ) {
        Map<CategoryType, ShoppingSearchQuery> queriesBySlot = new EnumMap<>(CategoryType.class);
        if (normalizedBlueprint == null || normalizedBlueprint.itemsBySlot() == null) {
            return queriesBySlot;
        }

        SearchQueryContext queryContext = buildQueryContext(brandEnabled);
        normalizedBlueprint.itemsBySlot().forEach((categoryType, item) -> {
            if (item == null) {
                return;
            }
            if (categoryType == normalizedBlueprint.anchorSlot()) {
                return;
            }

            String searchQuery = buildSearchQuery(
                categoryType,
                item,
                queryContext
            );
            if (searchQuery == null || searchQuery.isBlank()) {
                return;
            }

            queriesBySlot.put(
                categoryType,
                new ShoppingSearchQuery(searchQuery.trim(), shoppingProperties.getResultLimit())
            );
        });
        return queriesBySlot;
    }

    private String buildSearchQuery(
        CategoryType slotKey,
        RawBlueprintDto.ItemInfo item,
        SearchQueryContext queryContext
    ) {
        Set<String> mappedTokens = new LinkedHashSet<>();
        for (QueryTokenType tokenType : queryPolicy.tokenOrder(slotKey)) {
            addToken(mappedTokens, extractToken(slotKey, tokenType, item, queryContext));
        }
        return String.join(" ", mappedTokens);
    }

    private String extractToken(
        CategoryType slotKey,
        QueryTokenType tokenType,
        RawBlueprintDto.ItemInfo item,
        SearchQueryContext queryContext
    ) {
        if (tokenType == null) {
            return null;
        }
        return queryPolicy.extractToken(tokenType, slotKey, item, queryContext);
    }

    private void addToken(Set<String> tokens, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        tokens.add(value);
    }

    private SearchQueryContext buildQueryContext(Boolean brandEnabled) {
        return new SearchQueryContext(isBrandEnabled(brandEnabled));
    }

    private boolean isBrandEnabled(Boolean brandEnabled) {
        return brandEnabled == null || brandEnabled;
    }
}
