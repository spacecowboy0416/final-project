package com.finalproject.coordi.recommendation.infra.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QuerySortType;
import com.finalproject.coordi.recommendation.domain.enums.ShoppingQueryEnums.QueryTokenType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Naver Shopping 요청/응답 스키마와 한글 토큰 매핑 규칙을 보관한다.
 */
@Component
public class NaverShoppingSchemaProvider {
    public static final int RESULT_LIMIT = 5;
    public static final int START = 1;
    public static final QuerySortType SORT = QuerySortType.SIM;
    public static final List<QueryTokenType> TOKEN_ORDER = List.of(
        QueryTokenType.GENDER,
        QueryTokenType.COLOR,
        QueryTokenType.CATEGORY,
        QueryTokenType.FIT,
        QueryTokenType.MATERIAL
    );

    private static final NaverShoppingQuerySchema QUERY_SCHEMA = new NaverShoppingQuerySchema(
        RESULT_LIMIT,
        START,
        SORT,
        TOKEN_ORDER
    );

    public int resultLimit() {
        return QUERY_SCHEMA.resultLimit();
    }

    public int start() {
        return QUERY_SCHEMA.start();
    }

    public QuerySortType sort() {
        return QUERY_SCHEMA.sort();
    }

    public List<QueryTokenType> tokenOrder() {
        return QUERY_SCHEMA.tokenOrder();
    }

    public record NaverShoppingQuerySchema(
        int resultLimit,
        int start,
        QuerySortType sort,
        List<QueryTokenType> tokenOrder
    ) {
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

