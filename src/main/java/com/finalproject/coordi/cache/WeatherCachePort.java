package com.finalproject.coordi.cache;

import com.finalproject.coordi.main.dto.WeatherContextDto;

public interface WeatherCachePort {

    WeatherContextDto getWeather(String city, String gu);

    void cacheWeather(String city, String gu, WeatherContextDto dto);
}