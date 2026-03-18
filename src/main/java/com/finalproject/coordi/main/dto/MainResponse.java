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

    private String locationText;     
    private String locationSubText;  
    
    private String weatherStatus;
    private String weatherMain;      
    private String weatherStateKo;   
    private String weatherIcon;      
    private String weatherDesc;      
    
    private double temperature;
    private double feelsLike;
    private int humidity;
    private double windMs;
    private double precipMm;
    private boolean todayRain;

    private String fxMode;          

    private CategoryRecommendationDto top;
    private CategoryRecommendationDto bottom;
    private CategoryRecommendationDto outer;
    private CategoryRecommendationDto accessory;
}
