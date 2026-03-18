package com.finalproject.coordi.cache;

import com.finalproject.coordi.main.dto.WeatherContextDto;

/**
 * Redis를 사용하지 않을 때 사용하는 캐시 구현체
 * 실제로는 캐시를 사용하지 않고 항상 MISS처럼 동작한다.
 */
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