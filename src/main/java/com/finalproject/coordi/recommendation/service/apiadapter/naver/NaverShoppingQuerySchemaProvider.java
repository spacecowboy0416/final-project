package com.finalproject.coordi.recommendation.service.apiadapter.naver;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.CategoryKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.ColorKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.FitKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.GenderKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.MaterialKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QuerySortType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QueryTokenType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.StyleKeyword;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.TpoKeyword;
import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Naver Shopping 검색어 조합 순서와 한글 토큰 매핑 규칙을 보관한다.
 */
@Component
public class NaverShoppingQuerySchemaProvider {
    private static final NaverShoppingQuerySchema SCHEMA = new NaverShoppingQuerySchema(
        10,
        QuerySortType.SIM,
        List.of(
            QueryTokenType.GENDER,
            QueryTokenType.COLOR,
            QueryTokenType.CATEGORY,
            QueryTokenType.FIT,
            QueryTokenType.MATERIAL
        )
    );

    public int defaultResultLimit() {
        return SCHEMA.defaultResultLimit();
    }

    public QuerySortType defaultSort() {
        return SCHEMA.defaultSort();
    }

    public List<QueryTokenType> tokenOrder() {
        return SCHEMA.tokenOrder();
    }

    public String mapGender(BlueprintInputDto.GenderType gender) {
        return GenderKeyword.from(gender);
    }

    public String mapColor(ColorType colorType) {
        return ColorKeyword.from(colorType);
    }

    public String mapFit(FitType fitType) {
        return FitKeyword.from(fitType);
    }

    public String mapMaterial(MaterialType materialType) {
        return MaterialKeyword.from(materialType);
    }

    public String mapStyle(StyleType styleType) {
        return StyleKeyword.from(styleType);
    }

    public String mapCategory(ItemCategoryType categoryType) {
        return CategoryKeyword.from(categoryType);
    }

    public String mapTpo(TpoType tpoType) {
        return TpoKeyword.from(tpoType);
    }

    public record NaverShoppingQuerySchema(
        int defaultResultLimit,
        QuerySortType defaultSort,
        List<QueryTokenType> tokenOrder
    ) {
    }
}
