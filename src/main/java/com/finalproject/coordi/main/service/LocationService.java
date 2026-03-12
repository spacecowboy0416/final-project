package com.finalproject.coordi.main.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.finalproject.coordi.main.dto.KakaoCoord2RegionResponse;
import com.finalproject.coordi.main.dto.LocationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final RestTemplate restTemplate;

    @Value("${external.api.kakao.local.rest-api-key}")
    private String restApiKey;

    @Value("${external.api.kakao.local.coord2region-url}")
    private String coord2regionUrl;

    public LocationResponse getRegion(double lat, double lon) {
        URI uri = UriComponentsBuilder
                .fromUriString(coord2regionUrl)
                .queryParam("x", lon)
                .queryParam("y", lat)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoCoord2RegionResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                KakaoCoord2RegionResponse.class
        );

        KakaoCoord2RegionResponse body = response.getBody();

        if (body == null || body.getDocuments() == null || body.getDocuments().isEmpty()) {
            return new LocationResponse("서울", "", "");
        }

        KakaoCoord2RegionResponse.Document region = body.getDocuments().stream()
                .filter(doc -> "H".equals(doc.getRegionType()))
                .findFirst()
                .orElse(body.getDocuments().get(0));

        String city = normalizeCity(region.getRegion1depthName());
        String gu = safe(region.getRegion2depthName());
        String dong = safe(region.getRegion3depthName());

        return new LocationResponse(city, gu, dong);
    }

    private String normalizeCity(String city) {
        if (city == null || city.isBlank()) return "서울";

        return city
                .replace("특별시", "")
                .replace("광역시", "")
                .replace("특별자치시", "")
                .replace("특별자치도", "");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}