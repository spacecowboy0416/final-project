package com.finalproject.coordi.recommendation.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NaverShoppingAdapter implements ShoppingPort {
    private static final String DEFAULT_ENDPOINT = "https://openapi.naver.com/v1/search/shop.json";
    private static final String SOURCE = "NAVER";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${external.api.shopping.endpoint:" + DEFAULT_ENDPOINT + "}")
    private String endpoint;

    @Value("${external.api.shopping.client-id:}")
    private String clientId;

    @Value("${external.api.shopping.client-secret:}")
    private String clientSecret;

    public NaverShoppingAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 네이버 쇼핑 API를 호출해 상품 후보를 조회한다.
    @Override
    public List<ShoppingProductCandidate> search(ShoppingSearchQuery query) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            return List.of();
        }

        String encodedQuery = URLEncoder.encode(query.query(), StandardCharsets.UTF_8);
        String url = endpoint + "?query=" + encodedQuery + "&display=" + Math.max(1, query.display());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", clientId);
        headers.add("X-Naver-Client-Secret", clientSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return List.of();
        }
        return parseResponse(response.getBody());
    }

    // 응답 JSON에서 상품 후보 목록을 파싱한다.
    private List<ShoppingProductCandidate> parseResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode items = root.path("items");
            if (!items.isArray()) {
                return List.of();
            }

            List<ShoppingProductCandidate> result = new ArrayList<>();
            for (JsonNode item : items) {
                String externalId = item.path("productId").asText("");
                if (externalId.isBlank()) {
                    externalId = Integer.toHexString(item.path("link").asText("").hashCode());
                }

                int price = 0;
                String lprice = item.path("lprice").asText("0");
                try {
                    price = Integer.parseInt(lprice);
                } catch (NumberFormatException ignored) {
                }

                result.add(new ShoppingProductCandidate(
                    SOURCE,
                    externalId,
                    sanitizeTitle(item.path("title").asText("상품명 없음")),
                    item.path("brand").asText(""),
                    price,
                    item.path("image").asText(""),
                    item.path("link").asText("")
                ));
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    // HTML 태그가 포함된 상품명을 정리한다.
    private String sanitizeTitle(String raw) {
        return raw.replaceAll("<[^>]*>", "").trim();
    }
}
