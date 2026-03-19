package com.finalproject.coordi.recommendation.infra.navershopping.policy;

import com.finalproject.coordi.recommendation.domain.enums.CodedKeywordedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NaverShoppingQueryPolicy {
    private static final int START = 1;
    private static final QuerySortType SORT = QuerySortType.SIM;

    // 1. 슬롯별 토큰 순서 정의
    private static final List<QueryTokenType> TOPS_TOKEN_ORDER = List.of(
            QueryTokenType.GENDER, QueryTokenType.COLOR, QueryTokenType.CATEGORY, QueryTokenType.FIT,
            QueryTokenType.MATERIAL, QueryTokenType.BRAND, QueryTokenType.PATTERN, QueryTokenType.STYLE);

    private static final List<QueryTokenType> BOTTOMS_TOKEN_ORDER = List.of(
            QueryTokenType.GENDER, QueryTokenType.COLOR, QueryTokenType.CATEGORY, QueryTokenType.FIT,
            QueryTokenType.MATERIAL, QueryTokenType.BRAND, QueryTokenType.PATTERN, QueryTokenType.STYLE);

    private static final List<QueryTokenType> OUTERWEAR_TOKEN_ORDER = List.of(
            QueryTokenType.GENDER, QueryTokenType.COLOR, QueryTokenType.CATEGORY, QueryTokenType.FIT,
            QueryTokenType.MATERIAL, QueryTokenType.BRAND, QueryTokenType.PATTERN, QueryTokenType.STYLE);

    private static final List<QueryTokenType> HEADWEAR_TOKEN_ORDER = List.of(
            QueryTokenType.GENDER, QueryTokenType.COLOR, QueryTokenType.CATEGORY, QueryTokenType.MATERIAL,
            QueryTokenType.BRAND, QueryTokenType.PATTERN, QueryTokenType.STYLE);

    private static final List<QueryTokenType> SHOES_TOKEN_ORDER = List.of(
            QueryTokenType.GENDER, QueryTokenType.COLOR, QueryTokenType.CATEGORY, QueryTokenType.MATERIAL,
            QueryTokenType.STYLE);

    private static final List<QueryTokenType> ACCESSORIES_TOKEN_ORDER = List.of(
            QueryTokenType.GENDER, QueryTokenType.CATEGORY, QueryTokenType.COLOR, QueryTokenType.STYLE);

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

    // 2. 토큰 추출 핵심 로직
    public String extractToken(
            QueryTokenType tokenType,
            CategoryType slotKey,
            RawBlueprintDto.ItemInfo item,
            SearchQueryContext queryContext) {
        if (tokenType == null || item == null || item.attributes() == null) {
            return null;
        }

        return switch (tokenType) {
            case GENDER -> keywordOf(item.attributes().gender());
            case COLOR -> keywordOf(item.attributes().color());
            case CATEGORY -> keywordOf(item.category());
            case FIT -> keywordOf(item.attributes().fit());
            case MATERIAL -> materialKeywordOf(slotKey, item); // 소재 필터링 로직 호출
            case BRAND ->
                (queryContext != null && queryContext.brandEnabled()) ? keywordOf(item.attributes().brand()) : null;
            case PATTERN -> keywordOf(item.attributes().pattern());
            case STYLE -> keywordOf(item.attributes().style());
        };
    }

    // 3. 소재(Material) 특화 필터링 로직
    private String materialKeywordOf(CategoryType slotKey, RawBlueprintDto.ItemInfo item) {
        MaterialType material = item.attributes().material();
        if (material == null)
            return null;

        // 특정 카테고리에서 검색 효율이 떨어지는 소재는 키워드에서 제외한다 (Policy)
        boolean isSupported = switch (slotKey) {
            case SHOES -> material == MaterialType.LEATHER || material == MaterialType.SUEDE;
            case HEADWEAR -> EnumSet.of(
                    MaterialType.COTTON, MaterialType.DENIM, MaterialType.WOOL,
                    MaterialType.NYLON, MaterialType.FLEECE, MaterialType.CORDUROY).contains(material);
            case ACCESSORIES -> false; // 액세서리는 소재 검색 제외
            default -> true; // 상의, 하의, 아우터는 모든 소재 허용
        };

        return isSupported ? material.getKeyword() : null;
    }

    private String keywordOf(CodedKeywordedEnum source) {
        return (source == null) ? null : source.getKeyword();
    }

    // ==========================================
    // Enums & Records
    // ==========================================

    public enum QuerySortType {
        SIM("sim");

        private final String apiValue;

        QuerySortType(String apiValue) {
            this.apiValue = apiValue;
        }

        public String apiValue() {
            return apiValue;
        }
    }

    public enum QueryTokenType {
        GENDER, COLOR, CATEGORY, FIT, MATERIAL, BRAND, PATTERN, STYLE;
    }

    public record SearchQueryContext(boolean brandEnabled) {
    }
}
