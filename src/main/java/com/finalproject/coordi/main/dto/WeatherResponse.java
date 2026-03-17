package com.finalproject.coordi.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    private String locationText;

    private String weatherMainRaw;   
    private String weatherStatus;    

    private String conditionText;
    private String weatherStateKo;

    private double temperature;
    private double feelsLike;

    private int humidity;
    private double windMs;
    private double precipMm;

    private boolean todayRain;
}
