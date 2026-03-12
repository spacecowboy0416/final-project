package com.finalproject.coordi.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finalproject.coordi.main.dto.WeatherContextDto;
import com.finalproject.coordi.main.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "현재 위치 기준 날씨/위치 통합 정보 조회")
    @GetMapping
    public WeatherContextDto getWeather(
            @RequestParam(defaultValue = "37.5665") double lat,
            @RequestParam(defaultValue = "126.9780") double lon
    ) {
        return weatherService.getWeatherContext(lat, lon);
    }
}