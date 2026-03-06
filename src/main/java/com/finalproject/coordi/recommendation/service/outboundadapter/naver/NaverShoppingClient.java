package com.finalproject.coordi.recommendation.service.outboundadapter.naver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.exception.AppException;
import com.finalproject.coordi.recommendation.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 네이버 쇼핑 Open API와의 HTTP 통신만 담당하며, 검색 조건을 받아 네이버 전용 응답 DTO로 역직렬화해 반환하는 전용 클라이언트다.
 */
@Component
@RequiredArgsConstructor
public class NaverShoppingClient {
    private static final String NAVER_CLIENT_ID_HEADER = "X-Naver-Client-Id";
    private static final String NAVER_CLIENT_SECRET_HEADER = "X-Naver-Client-Secret";
    private static final int MIN_RESULT_LIMIT = 1;
    private static final int MAX_RESULT_LIMIT = 50;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.api.shopping.endpoint:https://openapi.naver.com/v1/search/shop.json}")
    private String shoppingEndpoint;

    @Value("${external.api.shopping.client-id:}")
    private String clientId;

    @Value("${external.api.shopping.client-secret:}")
    private String clientSecret;

    public NaverShoppingResponse search(String searchKeyword, int resultLimit) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new AppException(ErrorCode.EXTERNAL_API_ERROR, "Naver Shopping API 자격 정보가 비어 있습니다.");
        }

        String requestUri = UriComponentsBuilder.fromUriString(shoppingEndpoint)
            .queryParam("query", searchKeyword)
            .queryParam("display", clampResultLimit(resultLimit))
            .queryParam("sort", "sim")
            .build(true)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(NAVER_CLIENT_ID_HEADER, clientId);
        headers.set(NAVER_CLIENT_SECRET_HEADER, clientSecret);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                requestUri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                return new NaverShoppingResponse(List.of());
            }
            JsonNode responseRoot = objectMapper.readTree(responseBody);
            return NaverShoppingResponse.fromJson(responseRoot);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.EXTERNAL_API_ERROR, "Naver Shopping API 호출에 실패했습니다.", e);
        }
    }

    private int clampResultLimit(int requestedLimit) {
        return Math.max(MIN_RESULT_LIMIT, Math.min(requestedLimit, MAX_RESULT_LIMIT));
    }
}
