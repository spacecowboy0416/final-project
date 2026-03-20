package com.finalproject.coordi.recommendation.infra.navershopping;

import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.infra.navershopping.policy.NaverShoppingQueryPolicy;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Naver Shopping 검색 API를 호출해 상품 후보군을 recommendation 내부 후보 DTO로 변환한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverShoppingAdapter implements ShoppingPort {
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String PROTOCOL_RELATIVE_PREFIX = "//";

    private final NaverShoppingProperties shoppingProperties;
    private final NaverShoppingQueryPolicy queryPolicy;
    @Qualifier("naverShoppingRestClient")
    private final RestClient naverShoppingRestClient;

    @Override
    public List<SearchedProduct> search(ShoppingSearchQuery query) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        URI requestUri = UriComponentsBuilder.fromUriString(shoppingProperties.getEndpoint())
            .queryParam("query", query.searchKeyword())
            .queryParam("display", query.resultLimit())
            .queryParam("start", queryPolicy.start())
            .queryParam("sort", queryPolicy.sort().getApiValue())
            .encode()
            .build()
            .toUri();

        try {
            NaverShoppingSearchResponse response = naverShoppingRestClient.get()
                .uri(requestUri)
                .header("X-Naver-Client-Id", shoppingProperties.getClientId())
                .header("X-Naver-Client-Secret", shoppingProperties.getClientSecret())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.USER_AGENT, shoppingProperties.getUSER_AGENT())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, apiResponse) -> {
                    throw toApiCallException(apiResponse);
                })
                .body(NaverShoppingSearchResponse.class);

            List<SearchedProduct> searchedProducts = toSearchedProducts(response);
            stopWatch.stop();

            log.info(
                "naver shopping completed query={} requestedLimit={} searchedProductCount={} elapsedMs={} requestUrl={}",
                query.searchKeyword(),
                query.resultLimit(),
                searchedProducts.size(),
                stopWatch.getTotalTimeMillis(),
                requestUri
            );
            return searchedProducts;
        } catch (RecommendationException.AdapterException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RecommendationException.AdapterException(
                "Naver Shopping API 호출 중 예외 발생 [%s]".formatted(exception.getMessage()),
                com.finalproject.coordi.exception.ErrorCode.RECOMMENDATION_NAVER_SHOPPING_API_CALL_UNEXPECTED,
                exception
            );
        }
    }

    private RecommendationException.AdapterException toApiCallException(ClientHttpResponse apiResponse) {
        String responseBody = readResponseBody(apiResponse);
        String statusCode = readStatusCode(apiResponse);
        return new RecommendationException.AdapterException(
            "Naver Shopping API 호출 실패 [status=%s, body=%s]"
                .formatted(statusCode, abbreviate(responseBody)),
            com.finalproject.coordi.exception.ErrorCode.RECOMMENDATION_NAVER_SHOPPING_API_CALL_FAILED
        );
    }

    private List<SearchedProduct> toSearchedProducts(NaverShoppingSearchResponse response) {
        if (response == null || response.items() == null || response.items().isEmpty()) {
            return List.of();
        }

        List<SearchedProduct> searchedProducts = new ArrayList<>();
        for (NaverShoppingItemResponse item : response.items()) {
            searchedProducts.add(toSearchedProduct(item));
        }
        return searchedProducts;
    }

    private SearchedProduct toSearchedProduct(NaverShoppingItemResponse item) {
        return new SearchedProduct(
            shoppingProperties.getPROVIDER_NAME(),
            item.productId(),
            stripHtml(item.title()),
            item.brand(),
            parsePrice(item.lprice()),
            normalizeImageUrl(item.image()),
            item.link()
        );
    }

    private int parsePrice(String rawPrice) {
        if (!StringUtils.hasText(rawPrice)) {
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

    private String normalizeImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return imageUrl;
        }

        // HTTPS 화면에서 혼합 콘텐츠로 차단되지 않도록 네이버 이미지 URL 스킴을 정규화한다.
        if (imageUrl.startsWith(PROTOCOL_RELATIVE_PREFIX)) {
            return HTTPS_PREFIX + imageUrl.substring(PROTOCOL_RELATIVE_PREFIX.length());
        }
        if (imageUrl.startsWith(HTTP_PREFIX)) {
            return HTTPS_PREFIX + imageUrl.substring(HTTP_PREFIX.length());
        }
        return imageUrl;
    }

    private String readResponseBody(ClientHttpResponse response) {
        try {
            return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            return null;
        }
    }

    private String abbreviate(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        if (text.length() <= 500) {
            return text;
        }
        return text.substring(0, 500) + "...(truncated)";
    }

    private String readStatusCode(ClientHttpResponse response) {
        try {
            return String.valueOf(response.getStatusCode());
        } catch (Exception exception) {
            return "unknown";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NaverShoppingSearchResponse(
        List<NaverShoppingItemResponse> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NaverShoppingItemResponse(
        String productId,
        String title,
        String brand,
        String lprice,
        String image,
        String link
    ) {
    }
}