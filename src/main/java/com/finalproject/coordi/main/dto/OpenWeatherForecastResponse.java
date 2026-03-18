package com.finalproject.coordi.main.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherForecastResponse {

    private List<ForecastItem> list;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastItem {

        private long dt;
        private List<Weather> weather;

        private Map<String, Double> rain;
        private Map<String, Double> snow;

        private double pop;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {

        private String main;
        private String description;
    }
}