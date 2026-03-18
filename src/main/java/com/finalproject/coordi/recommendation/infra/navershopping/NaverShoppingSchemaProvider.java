package com.finalproject.coordi.recommendation.infra.navershopping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QuerySortType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QueryTokenType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Naver Shopping 요청/응답 스키마와 한글 토큰 매핑 규칙을 보관한다.
 */
@Component
@RequiredArgsConstructor
public class NaverShoppingSchemaProvider {
    public static final int START = 1;
    public static final QuerySortType SORT = QuerySortType.SIM;
    public static final List<QueryTokenType> TOKEN_ORDER = List.of(
        QueryTokenType.GENDER,
        QueryTokenType.COLOR,
        QueryTokenType.CATEGORY,
        QueryTokenType.FIT,
        QueryTokenType.MATERIAL
    );

    private final NaverShoppingProperties shoppingProperties;

    public int resultLimit() {
        return shoppingProperties.getResultLimit();
    }

    public int start() {
        return START;
    }

    public QuerySortType sort() {
        return SORT;
    }

    public List<QueryTokenType> tokenOrder() {
        return TOKEN_ORDER;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverShoppingSearchResponse(
        List<NaverShoppingItemResponse> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverShoppingItemResponse(
        String productId,
        String title,
        String brand,
        String lprice,
        String image,
        String link
    ) {
    }
}
