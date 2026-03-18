package com.finalproject.coordi.cache;

import com.finalproject.coordi.main.dto.WeatherContextDto;

/**
 * 날씨 캐시 추상화 포트
 * Redis 사용 여부와 상관없이 WeatherService는 이 인터페이스만 의존한다.
 */
public interface WeatherCachePort {

    WeatherContextDto getWeather(String city, String gu);

    void cacheWeather(String city, String gu, WeatherContextDto dto);
}