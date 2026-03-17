package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.CategoryKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.ColorKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.FitKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.MaterialKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QueryTokenType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.StyleKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.TpoKeyword;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.infra.naver.NaverShoppingSchemaProvider;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.ShoppingSearchQuery;
import java.util.LinkedHashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryExtractor {
    private final NaverShoppingSchemaProvider schemaProvider;

    public Map<CategoryType, ShoppingSearchQuery> extract(NormalizedBlueprintDto validatedBlueprint) {
        Map<CategoryType, ShoppingSearchQuery> queriesBySlot = new EnumMap<>(CategoryType.class);
        if (validatedBlueprint == null || validatedBlueprint.itemsBySlot() == null) {
            return queriesBySlot;
        }

        validatedBlueprint.itemsBySlot().forEach((categoryType, item) -> {
            if (item == null) {
                return;
            }

            String searchQuery = buildSearchQuery(
                categoryType,
                item,
                validatedBlueprint.aiBlueprint().styleType(),
                validatedBlueprint.aiBlueprint().tpoType()
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
        StyleType styleType,
        TpoType tpoType
    ) {
        Set<String> mappedTokens = new LinkedHashSet<>();
        for (QueryTokenType tokenType : schemaProvider.tokenOrder()) {
            addToken(mappedTokens, resolveToken(slotKey, tokenType, item, styleType, tpoType));
        }
        return String.join(" ", mappedTokens);
    }

    private String resolveToken(
        CategoryType slotKey,
        QueryTokenType tokenType,
        RawBlueprintDto.ItemInfo item,
        StyleType styleType,
        TpoType tpoType
    ) {
        if (tokenType == null) {
            return null;
        }
        return switch (tokenType) {
            case GENDER -> null;
            case COLOR -> item == null || item.attributes() == null ? null : ColorKeyword.from(item.attributes().color());
            case FIT -> supportsFit(slotKey) && item != null && item.attributes() != null
                ? FitKeyword.from(item.attributes().fit())
                : null;
            case MATERIAL -> supportsMaterial(slotKey) && item != null && item.attributes() != null
                ? MaterialKeyword.from(item.attributes().material())
                : null;
            case STYLE -> {
                StyleType resolvedStyle = item != null && item.attributes() != null ? item.attributes().style() : styleType;
                yield StyleKeyword.from(resolvedStyle);
            }
            case CATEGORY -> CategoryKeyword.from(item == null ? null : item.category());
            case TPO -> TpoKeyword.from(tpoType);
        };
    }

    private boolean supportsFit(CategoryType slotKey) {
        return slotKey != CategoryType.SHOES && slotKey != CategoryType.ACCESSORIES;
    }

    private boolean supportsMaterial(CategoryType slotKey) {
        return slotKey != CategoryType.ACCESSORIES;
    }

    private void addToken(Set<String> tokens, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        tokens.add(value);
    }
}
