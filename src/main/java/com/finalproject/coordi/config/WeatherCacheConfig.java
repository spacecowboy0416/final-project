package com.finalproject.coordi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.cache.NoOpWeatherCacheAdapter;
import com.finalproject.coordi.cache.RedisWeatherCacheAdapter;
import com.finalproject.coordi.cache.WeatherCachePort;

@Configuration
public class WeatherCacheConfig {

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public WeatherCachePort redisWeatherCachePort(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new RedisWeatherCacheAdapter(redisTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(WeatherCachePort.class)
    public WeatherCachePort noOpWeatherCachePort() {
        return new NoOpWeatherCacheAdapter();
    }
}