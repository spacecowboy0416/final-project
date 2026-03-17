package com.finalproject.coordi.cache;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.main.dto.WeatherContextDto;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 날씨 캐시 구현체
 * city + gu 기준으로 날씨 정보를 Redis에 저장하고 재사용한다.
 */
@RequiredArgsConstructor
public class RedisWeatherCacheAdapter implements WeatherCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisWeatherCacheAdapter.class);
    // 날씨 캐시 TTL: 30분
    private static final Duration WEATHER_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void cacheWeather(String city, String gu, WeatherContextDto dto) {
        String key = buildKey(city, gu);
        redisTemplate.opsForValue().set(key, dto, WEATHER_TTL);

        log.info("[WEATHER CACHE PUT] key={}, ttlMinutes={}", key, WEATHER_TTL.toMinutes());
    }

    @Override
    public WeatherContextDto getWeather(String city, String gu) {
        String key = buildKey(city, gu);
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            log.info("[WEATHER CACHE MISS] key={}", key);
            return null;
        }

        log.info("[WEATHER CACHE HIT] key={}", key);

        if (value instanceof WeatherContextDto dto) {
            return dto;
        }

        return objectMapper.convertValue(value, WeatherContextDto.class);
    }

    private String buildKey(String city, String gu) {
        return "weather:" + normalize(city, "서울") + ":" + normalize(gu, "unknown");
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim()
                .replace(" ", "")
                .replace("특별시", "")
                .replace("광역시", "")
                .replace("특별자치시", "")
                .replace("특별자치도", "");
    }
}