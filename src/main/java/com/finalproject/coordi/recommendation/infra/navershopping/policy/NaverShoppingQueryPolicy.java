package com.finalproject.coordi.recommendation.infra.navershopping.policy;

import com.finalproject.coordi.recommendation.domain.enums.CodedKeywordedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static com.finalproject.coordi.recommendation.infra.navershopping.policy.NaverShoppingQueryPolicy.QueryTokenType.*;

@Component
public class NaverShoppingQueryPolicy {
    private static final int START = 1;
    private static final QuerySortType SORT = QuerySortType.SIM;

    private static final List<QueryTokenType> HEADWEAR_TOKEN_ORDER = List.of(
            BRAND, COLOR, CATEGORY);

    private static final List<QueryTokenType> TOPS_TOKEN_ORDER = List.of(
            BRAND, GENDER, COLOR, CATEGORY);

    private static final List<QueryTokenType> BOTTOMS_TOKEN_ORDER = List.of(
            BRAND, GENDER, COLOR, CATEGORY);

    private static final List<QueryTokenType> OUTERWEAR_TOKEN_ORDER = List.of(
            BRAND, GENDER, COLOR, CATEGORY);

    private static final List<QueryTokenType> SHOES_TOKEN_ORDER = List.of(
            BRAND, COLOR, CATEGORY);

    private static final List<QueryTokenType> ACCESSORIES_TOKEN_ORDER = List.of(
            COLOR, CATEGORY);

    public int start() {
        return START;
    }

    public QuerySortType sort() {
        return SORT;
    }

    public List<QueryTokenType> tokenOrder(CategoryType slotKey) {
        if (slotKey == null)
            return TOPS_TOKEN_ORDER;
        return switch (slotKey) {
            case TOPS -> TOPS_TOKEN_ORDER;
            case BOTTOMS -> BOTTOMS_TOKEN_ORDER;
            case OUTERWEAR -> OUTERWEAR_TOKEN_ORDER;
            case HEADWEAR -> HEADWEAR_TOKEN_ORDER;
            case SHOES -> SHOES_TOKEN_ORDER;
            case ACCESSORIES -> ACCESSORIES_TOKEN_ORDER;
        };
    }

    // 2. 토큰 추출 핵심 로직 (여기서도 GENDER, COLOR 등으로 직접 비교 가능)
    public String extractToken(
            QueryTokenType tokenType,
            CategoryType slotKey,
            RawBlueprintDto.ItemInfo item,
            SearchQueryContext queryContext) {

        if (tokenType == null || item == null || item.attributes() == null)
            return null;

        return switch (tokenType) {
            case GENDER -> keywordOf(item.attributes().gender());
            case COLOR -> keywordOf(item.attributes().color());
            case CATEGORY -> keywordOf(item.category());
            case FIT -> keywordOf(item.attributes().fit());
            case MATERIAL -> materialKeywordOf(slotKey, item);
            case BRAND -> (queryContext != null && queryContext.brandEnabled())
                    ? keywordOf(item.attributes().brand())
                    : null;
            case PATTERN -> keywordOf(item.attributes().pattern());
            case STYLE -> keywordOf(item.attributes().style());
        };
    }

    private String materialKeywordOf(CategoryType slotKey, RawBlueprintDto.ItemInfo item) {
        MaterialType material = item.attributes().material();
        if (material == null)
            return null;

        boolean isSupported = switch (slotKey) {
            case SHOES -> material == MaterialType.LEATHER || material == MaterialType.SUEDE;
            case HEADWEAR -> EnumSet.of(
                    MaterialType.COTTON, MaterialType.DENIM, MaterialType.WOOL,
                    MaterialType.NYLON, MaterialType.FLEECE, MaterialType.CORDUROY).contains(material);
            case ACCESSORIES -> false;
            default -> true;
        };

        return isSupported ? material.getKeyword() : null;
    }

    private String keywordOf(CodedKeywordedEnum source) {
        return (source == null) ? null : source.getKeyword();
    }

    @Getter
    @RequiredArgsConstructor
    public enum QuerySortType {
        SIM("sim");

        private final String apiValue;
    }

    public enum QueryTokenType {
        GENDER, COLOR, CATEGORY, FIT, MATERIAL, BRAND, PATTERN, STYLE;
    }

    public record SearchQueryContext(boolean brandEnabled) {
    }
}