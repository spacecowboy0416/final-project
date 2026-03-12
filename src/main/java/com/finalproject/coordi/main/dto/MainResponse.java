package com.finalproject.coordi.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainResponse {

    private String locationText;     // 예: 서울 강남구 역삼동
    private String locationSubText;  // 예: 현재 위치 / 기본 위치
    
    private String weatherStatus;
    private String weatherMain;      // Clear, Clouds ...
    private String weatherStateKo;   // 맑음, 흐림 ...
    private String weatherIcon;      // ☼, ☁ ...
    private String weatherDesc;      // 설명문

    private double temperature;
    private double feelsLike;
    private int humidity;
    private double windMs;
    private double precipMm;
    private boolean todayRain;

    private String fxMode;           // sun, rain, thunder, snow, none

    private CategoryRecommendationDto top;
    private CategoryRecommendationDto bottom;
    private CategoryRecommendationDto outer;
    private CategoryRecommendationDto accessory;
}
