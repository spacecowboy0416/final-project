package com.finalproject.coordi.recommendation.service.apiadapter.kakaomap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.service.apiport.MapPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Kakao Local API(coord2address) 어댑터.
 */
@Component
@RequiredArgsConstructor
public class KakaoMapAdapter implements MapPort {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.api.kakao-map.endpoint:https://dapi.kakao.com/v2/local/geo/coord2address.json}")
    private String kakaoMapEndpoint;

    @Value("${external.api.kakao-map.key:}")
    private String kakaoMapApiKey;

    @Value("${external.api.kakao-map.auth-scheme}")
    private String kakaoMapAuthScheme;

    @Override
    // 좌표를 Kakao API로 해석하고 실패 시 요청 원본값으로 폴백한다.
    public LocationMeta resolveLocation(
        double latitude,
        double longitude,
        String fallbackPlaceName,
        String fallbackAddressName
    ) {
        if (kakaoMapApiKey == null || kakaoMapApiKey.isBlank()) {
            return new LocationMeta(
                safeText(fallbackPlaceName),
                safeText(fallbackAddressName),
                latitude,
                longitude
            );
        }

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(kakaoMapEndpoint)
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .build()
                .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, kakaoMapAuthScheme + " " + kakaoMapApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            ).getBody();

            if (body == null || body.isBlank()) {
                return new LocationMeta(
                    safeText(fallbackPlaceName),
                    safeText(fallbackAddressName),
                    latitude,
                    longitude
                );
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode first = root.path("documents").isArray() && root.path("documents").size() > 0
                ? root.path("documents").get(0)
                : null;
            if (first == null) {
                return new LocationMeta(
                    safeText(fallbackPlaceName),
                    safeText(fallbackAddressName),
                    latitude,
                    longitude
                );
            }

            String roadAddress = first.path("road_address").path("address_name").asText("");
            String lotAddress = first.path("address").path("address_name").asText("");
            String resolvedAddress = !roadAddress.isBlank() ? roadAddress : lotAddress;
            String resolvedPlace = !roadAddress.isBlank() ? roadAddress : safeText(fallbackPlaceName);

            return new LocationMeta(
                !resolvedPlace.isBlank() ? resolvedPlace : safeText(fallbackPlaceName),
                !resolvedAddress.isBlank() ? resolvedAddress : safeText(fallbackAddressName),
                latitude,
                longitude
            );
        } catch (Exception ignored) {
            return new LocationMeta(
                safeText(fallbackPlaceName),
                safeText(fallbackAddressName),
                latitude,
                longitude
            );
        }
    }

    // null 문자열을 빈 문자열로 정규화한다.
    private String safeText(String text) {
        return text == null ? "" : text;
    }
}


