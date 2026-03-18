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

/**
 * 실행 환경에 따라 날씨 캐시 구현체를 선택한다.
 * - RedisTemplate이 있으면 RedisWeatherCacheAdapter 사용
 * - 없으면 NoOpWeatherCacheAdapter 사용
 */
@Configuration
public class WeatherCacheConfig {

	/**
	 * Redis가 활성화된 경우 실제 Redis 캐시 어댑터를 사용한다.
	 */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public WeatherCachePort redisWeatherCachePort(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        return new RedisWeatherCacheAdapter(redisTemplate, objectMapper);
    }
    
    /**
     * Redis를 사용하지 않는 경우 NoOp 캐시 어댑터를 사용한다.
     * 캐시 기능은 비활성화되지만 서버는 정상 동작한다.
     */
    @Bean
    @ConditionalOnMissingBean(WeatherCachePort.class)
    public WeatherCachePort noOpWeatherCachePort() {
        return new NoOpWeatherCacheAdapter();
    }
}