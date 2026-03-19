package com.finalproject.coordi.recommendation.infra.navershopping.policy;

import com.finalproject.coordi.recommendation.domain.enums.CodedKeywordedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Naver Shopping 검색어 조합 정책을 한곳에 모은다.
 */
@Component
public class NaverShoppingQueryPolicy {
    private static final int START = 1;
    private static final QuerySortType SORT = QuerySortType.SIM;
    private static final List<QueryTokenType> DEFAULT_TOKEN_ORDER = List.of(
        QueryTokenType.GENDER,
        QueryTokenType.COLOR,
        QueryTokenType.CATEGORY,
        QueryTokenType.FIT,
        QueryTokenType.MATERIAL,
        QueryTokenType.STYLE
    );
    private static final List<QueryTokenType> HEADWEAR_TOKEN_ORDER = List.of(
        QueryTokenType.GENDER,
        QueryTokenType.COLOR,
        QueryTokenType.CATEGORY,
        QueryTokenType.MATERIAL,
        QueryTokenType.STYLE
    );
    private static final List<QueryTokenType> SHOES_TOKEN_ORDER = List.of(
        QueryTokenType.GENDER,
        QueryTokenType.COLOR,
        QueryTokenType.CATEGORY,
        QueryTokenType.MATERIAL,
        QueryTokenType.STYLE
    );
    private static final List<QueryTokenType> ACCESSORIES_TOKEN_ORDER = List.of(
        QueryTokenType.GENDER,
        QueryTokenType.CATEGORY,
        QueryTokenType.COLOR,
        QueryTokenType.STYLE
    );
    private static final EnumSet<CategoryType> FIT_SUPPORTED_SLOTS = EnumSet.of(
        CategoryType.TOPS,
        CategoryType.BOTTOMS,
        CategoryType.OUTERWEAR
    );
    private static final EnumSet<CategoryType> MATERIAL_SUPPORTED_SLOTS = EnumSet.of(
        CategoryType.HEADWEAR,
        CategoryType.TOPS,
        CategoryType.BOTTOMS,
        CategoryType.OUTERWEAR,
        CategoryType.SHOES
    );

    public int start() {
        return START;
    }

    public QuerySortType sort() {
        return SORT;
    }

    public List<QueryTokenType> tokenOrder(CategoryType slotKey) {
        if (slotKey == null) {
            return DEFAULT_TOKEN_ORDER;
        }
        return switch (slotKey) {
            case HEADWEAR -> HEADWEAR_TOKEN_ORDER;
            case SHOES -> SHOES_TOKEN_ORDER;
            case ACCESSORIES -> ACCESSORIES_TOKEN_ORDER;
            default -> DEFAULT_TOKEN_ORDER;
        };
    }

    public String extractToken(
        QueryTokenType tokenType,
        CategoryType slotKey,
        RawBlueprintDto.ItemInfo item,
        SearchQueryContext queryContext
    ) {
        if (tokenType == null || !tokenType.isSupported(slotKey)) {
            return null;
        }

        return switch (tokenType) {
            case GENDER -> keywordOf(queryContext == null ? null : queryContext.gender());
            case STYLE -> keywordOf(
                item != null && item.attributes() != null
                    ? item.attributes().style()
                    : queryContext == null ? null : queryContext.styleType()
            );
            case COLOR -> keywordOf(item == null || item.attributes() == null ? null : item.attributes().color());
            case CATEGORY -> keywordOf(item == null ? null : item.category());
            case FIT -> keywordOf(item == null || item.attributes() == null ? null : item.attributes().fit());
            case MATERIAL -> materialKeywordOf(slotKey, item);
            case TPO -> keywordOf(queryContext == null ? null : queryContext.tpoType());
        };
    }

    private String materialKeywordOf(CategoryType slotKey, RawBlueprintDto.ItemInfo item) {
        MaterialType materialType = item == null || item.attributes() == null ? null : item.attributes().material();
        if (materialType == null || !isMaterialKeywordSupported(slotKey, materialType)) {
            return null;
        }
        return materialType.getKeyword();
    }

    private boolean isMaterialKeywordSupported(CategoryType slotKey, MaterialType materialType) {
        if (slotKey == null || materialType == null) {
            return false;
        }
        return switch (slotKey) {
            case SHOES -> materialType == MaterialType.LEATHER || materialType == MaterialType.SUEDE;
            case HEADWEAR -> EnumSet.of(
                MaterialType.COTTON,
                MaterialType.DENIM,
                MaterialType.WOOL,
                MaterialType.NYLON,
                MaterialType.FLEECE,
                MaterialType.CORDUROY
            ).contains(materialType);
            default -> true;
        };
    }

    private String keywordOf(CodedKeywordedEnum source) {
        if (source == null) {
            return null;
        }
        return source.getKeyword();
    }

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
        GENDER,
        STYLE,
        COLOR,
        CATEGORY,
        FIT,
        MATERIAL,
        TPO;

        public boolean isSupported(CategoryType slotKey) {
            if (slotKey == null) {
                return true;
            }
            return switch (this) {
                case FIT -> FIT_SUPPORTED_SLOTS.contains(slotKey);
                case MATERIAL -> MATERIAL_SUPPORTED_SLOTS.contains(slotKey);
                default -> true;
            };
        }
    }

    public record SearchQueryContext(
        GenderType gender,
        StyleType styleType,
        TpoType tpoType
    ) {
    }
}
