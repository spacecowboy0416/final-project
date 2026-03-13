package com.finalproject.coordi.cache;

import com.finalproject.coordi.main.dto.WeatherContextDto;

public class NoOpWeatherCacheAdapter implements WeatherCachePort {

    @Override
    public WeatherContextDto getWeather(String city, String gu) {
        return null;
    }

    @Override
    public void cacheWeather(String city, String gu, WeatherContextDto dto) {
        // Redis 캐시 비활성화 상태이므로 아무 것도 하지 않음
    }
}