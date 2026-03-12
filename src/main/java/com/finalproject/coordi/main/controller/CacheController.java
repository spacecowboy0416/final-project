package com.finalproject.coordi.main.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cache")
public class CacheController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/weather")
    public String clearWeatherCache() {

        Set<String> keys = redisTemplate.keys("weather:*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        return "weather cache cleared";
    }
    
    @GetMapping("/weather/list")
    public Object getWeatherCache() {

        Set<String> keys = redisTemplate.keys("weather:*");

        if (keys == null || keys.isEmpty()) {
            return "no cache";
        }

        Map<String, Object> result = new HashMap<>();

        for (String key : keys) {
            result.put(key, redisTemplate.opsForValue().get(key));
        }

        return result;
    }
}