package com.finalproject.coordi.main.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.finalproject.coordi.exception.main.CurrentWeatherResponseNullException;
import com.finalproject.coordi.exception.main.ForecastWeatherResponseNullException;
import com.finalproject.coordi.main.dto.OpenWeatherForecastResponse;
import com.finalproject.coordi.main.dto.OpenWeatherResponse;

import lombok.RequiredArgsConstructor;

/**
 * OpenWeather API 호출 전용 클라이언트
 * 현재 날씨와 예보 데이터를 각각 조회한다.
 */
@Component
@RequiredArgsConstructor
public class OpenWeatherClient {

    private final RestTemplate restTemplate;

    @Value("${external.api.weather.key}")
    private String apiKey;

    @Value("${external.api.weather.current-url}")
    private String currentUrl;

    @Value("${external.api.weather.forecast-url}")
    private String forecastUrl;
    
    //현재 날씨 조회
    public OpenWeatherResponse getCurrentWeather(double lat, double lon) {

        URI uri = UriComponentsBuilder
                .fromUriString(currentUrl)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        OpenWeatherResponse response =
                restTemplate.getForObject(uri, OpenWeatherResponse.class);

        if (response == null) {
            throw new CurrentWeatherResponseNullException();
        }

        return response;
    }
    //예보 날씨 조회
    public OpenWeatherForecastResponse getForecast(double lat, double lon) {

        URI uri = UriComponentsBuilder
                .fromUriString(forecastUrl)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .toUri();

        OpenWeatherForecastResponse response =
                restTemplate.getForObject(uri, OpenWeatherForecastResponse.class);

        if (response == null) {
            throw new ForecastWeatherResponseNullException();
        }

        return response;
    }
}