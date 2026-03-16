package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QueryTokenType;
import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.dto.internal.BlueprintValidationDto;
import com.finalproject.coordi.recommendation.service.apiadapter.naver.NaverShoppingQuerySchemaProvider;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingSearchQuery;
import java.util.LinkedHashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemSearchQueryExtractor {
    private final NaverShoppingQuerySchemaProvider querySchemaProvider;

    public Map<CategoryType, ShoppingSearchQuery> extract(
        BlueprintValidationDto.ValidatedBlueprint validatedBlueprint,
        BlueprintInputDto request
    ) {
        Map<CategoryType, ShoppingSearchQuery> queriesBySlot = new EnumMap<>(CategoryType.class);
        if (validatedBlueprint == null || validatedBlueprint.slotsByCategory() == null) {
            return queriesBySlot;
        }

        validatedBlueprint.slotsByCategory().forEach((categoryType, slot) -> {
            if (slot == null || slot.raw() == null) {
                return;
            }

            String searchQuery = buildSearchQuery(slot.raw(), validatedBlueprint.aiBlueprint().styleType(), validatedBlueprint.aiBlueprint().tpoType(), request);
            if (searchQuery == null || searchQuery.isBlank()) {
                return;
            }

            queriesBySlot.put(
                categoryType,
                new ShoppingSearchQuery(searchQuery.trim(), querySchemaProvider.defaultResultLimit())
            );
        });
        return queriesBySlot;
    }

    private String buildSearchQuery(
        BlueprintOutputDto.CoordinationSlot slot,
        StyleType styleType,
        TpoType tpoType,
        BlueprintInputDto request
    ) {
        Set<String> mappedTokens = new LinkedHashSet<>();
        for (QueryTokenType tokenType : querySchemaProvider.tokenOrder()) {
            addToken(mappedTokens, resolveToken(tokenType, slot, styleType, tpoType, request));
        }
        return String.join(" ", mappedTokens);
    }

    private String resolveToken(
        QueryTokenType tokenType,
        BlueprintOutputDto.CoordinationSlot slot,
        StyleType styleType,
        TpoType tpoType,
        BlueprintInputDto request
    ) {
        return switch (tokenType) {
            case GENDER -> querySchemaProvider.mapGender(request == null ? null : request.gender());
            case COLOR -> slot == null || slot.attributes() == null ? null : querySchemaProvider.mapColor(slot.attributes().color());
            case FIT -> slot == null || slot.attributes() == null ? null : querySchemaProvider.mapFit(slot.attributes().fit());
            case MATERIAL -> slot == null || slot.attributes() == null ? null : querySchemaProvider.mapMaterial(slot.attributes().material());
            case STYLE -> {
                StyleType resolvedStyle = slot != null && slot.attributes() != null ? slot.attributes().style() : styleType;
                yield querySchemaProvider.mapStyle(resolvedStyle);
            }
            case CATEGORY -> querySchemaProvider.mapCategory(slot == null ? null : slot.category());
            case TPO -> querySchemaProvider.mapTpo(tpoType);
        };
    }

    private void addToken(Set<String> tokens, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        tokens.add(value);
    }
}

