package com.finalproject.coordi.recommendation.infra.navershopping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Naver Shopping 요청/응답 스키마와 한글 토큰 매핑 규칙을 보관한다.
 */
@Component
@RequiredArgsConstructor
public class NaverShoppingSchemaProvider {
    private final NaverShoppingProperties shoppingProperties;

    public int resultLimit() {
        return shoppingProperties.getResultLimit();
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
