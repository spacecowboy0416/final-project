package com.finalproject.coordi.recommendation.service.apiadapter.naver;

import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Naver Shopping 검색 API를 호출해 상품 후보군을 recommendation 내부 후보 DTO로 변환한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverShoppingAdapter implements ShoppingPort {
    private static final String PROVIDER_NAME = "naver_shopping";

    private final NaverShoppingProperties shoppingProperties;
    private final NaverShoppingQuerySchemaProvider querySchemaProvider;
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public List<ShoppingProductCandidate> search(ShoppingSearchQuery query) {
        validateConfiguration();

        String requestUrl = UriComponentsBuilder.fromUriString(shoppingProperties.getEndpoint())
            .queryParam("query", query.searchKeyword())
            .queryParam("display", query.resultLimit())
            .queryParam("start", 1)
            .queryParam("sort", querySchemaProvider.defaultSort().apiValue())
            .encode()
            .build()
            .toUriString();

        try {
            NaverShoppingSearchResponse response = restClient.get()
                .uri(requestUrl)
                .header("X-Naver-Client-Id", shoppingProperties.getClientId())
                .header("X-Naver-Client-Secret", shoppingProperties.getClientSecret())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .body(NaverShoppingSearchResponse.class);

            List<ShoppingProductCandidate> candidates = response == null || response.items() == null
                ? List.of()
                : response.items().stream()
                    .map(this::toCandidate)
                    .toList();

            log.info(
                "naver shopping completed query={} requestedLimit={} candidateCount={}",
                query.searchKeyword(),
                query.resultLimit(),
                candidates.size()
            );
            return candidates;
        } catch (RestClientResponseException exception) {
            log.error("Naver Shopping API Error status={} body={}", exception.getStatusCode(), exception.getResponseBodyAsString(), exception);
            throw new RecommendationException.AdapterException(
                "Naver Shopping API 호출 실패 [status=%s]".formatted(exception.getStatusCode()),
                com.finalproject.coordi.exception.ErrorCode.RECOMMENDATION_NAVER_SHOPPING_API_CALL_FAILED,
                exception
            );
        } catch (Exception exception) {
            log.error("Naver Shopping API unexpected error query={}", query.searchKeyword(), exception);
            throw new RecommendationException.AdapterException(
                "Naver Shopping API 호출 중 예외 발생 [%s]".formatted(exception.getMessage()),
                com.finalproject.coordi.exception.ErrorCode.RECOMMENDATION_NAVER_SHOPPING_API_CALL_UNEXPECTED,
                exception
            );
        }
    }

    private void validateConfiguration() {
        if (!hasText(shoppingProperties.getEndpoint())
            || !hasText(shoppingProperties.getClientId())
            || !hasText(shoppingProperties.getClientSecret())) {
            throw new RecommendationException.AdapterException(
                "Naver Shopping 설정이 누락되었습니다.",
                com.finalproject.coordi.exception.ErrorCode.RECOMMENDATION_NAVER_SHOPPING_CONFIG_MISSING
            );
        }
    }

    private ShoppingProductCandidate toCandidate(NaverShoppingItem item) {
        return new ShoppingProductCandidate(
            PROVIDER_NAME,
            item.productId(),
            stripHtml(item.title()),
            item.brand(),
            parsePrice(item.lprice()),
            item.image(),
            item.link()
        );
    }

    private int parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(rawPrice);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String stripHtml(String value) {
        return value == null ? null : Jsoup.parse(value).text();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record NaverShoppingSearchResponse(
        List<NaverShoppingItem> items
    ) {
    }

    private record NaverShoppingItem(
        String title,
        String link,
        String image,
        String lprice,
        String productId,
        String brand
    ) {
    }
}
