package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QueryTokenType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.infra.navershopping.NaverShoppingSchemaProvider;
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
    private final NaverShoppingSchemaProvider schemaProvider;

    public Map<CategoryType, ShoppingSearchQuery> extract(NormalizedBlueprintDto normalizedBlueprint) {
        Map<CategoryType, ShoppingSearchQuery> queriesBySlot = new EnumMap<>(CategoryType.class);
        if (normalizedBlueprint == null || normalizedBlueprint.itemsBySlot() == null) {
            return queriesBySlot;
        }

        GenderType gender = resolveGender(normalizedBlueprint);
        normalizedBlueprint.itemsBySlot().forEach((categoryType, item) -> {
            if (item == null) {
                return;
            }

            String searchQuery = buildSearchQuery(
                categoryType,
                item,
                gender,
                normalizedBlueprint.aiBlueprint().styleType(),
                normalizedBlueprint.aiBlueprint().tpoType()
            );
            if (searchQuery == null || searchQuery.isBlank()) {
                return;
            }

            queriesBySlot.put(
                categoryType,
                new ShoppingSearchQuery(searchQuery.trim(), schemaProvider.resultLimit())
            );
        });
        return queriesBySlot;
    }

    private String buildSearchQuery(
        CategoryType slotKey,
        RawBlueprintDto.ItemInfo item,
        GenderType gender,
        StyleType styleType,
        TpoType tpoType
    ) {
        Set<String> mappedTokens = new LinkedHashSet<>();
        for (QueryTokenType tokenType : schemaProvider.tokenOrder()) {
            addToken(mappedTokens, extractToken(slotKey, tokenType, item, gender, styleType, tpoType));
        }
        return String.join(" ", mappedTokens);
    }

    private String extractToken(
        CategoryType slotKey,
        QueryTokenType tokenType,
        RawBlueprintDto.ItemInfo item,
        GenderType gender,
        StyleType styleType,
        TpoType tpoType
    ) {
        if (tokenType == null) {
            return null;
        }
        return tokenType.extractToken(slotKey, item, gender, styleType, tpoType);
    }

    private void addToken(Set<String> tokens, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        tokens.add(value);
    }

    private GenderType resolveGender(NormalizedBlueprintDto normalizedBlueprint) {
        if (normalizedBlueprint == null || normalizedBlueprint.aiBlueprint() == null) {
            return null;
        }
        return normalizedBlueprint.aiBlueprint().gender();
    }
}
