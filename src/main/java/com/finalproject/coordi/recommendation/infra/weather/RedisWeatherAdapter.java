package com.finalproject.coordi.recommendation.infra.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.WeatherMappingKeyPolicy;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherSourceType;
import com.finalproject.coordi.recommendation.service.payload.WeatherPort;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis에 캐싱된 구/군 단위 날씨 정보를 읽어 recommendation 파이프라인에서 사용할 형태로 변환하는 어댑터.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cache.redis-enabled", havingValue = "true")
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisWeatherAdapter implements WeatherPort {
    private static final Logger log = LoggerFactory.getLogger(RedisWeatherAdapter.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherMappingKeyPolicy weatherMappingKeyPolicy;

    /**
     * districtName과 scheduleTime을 Redis 키로 조합해 캐시된 날씨 JSON을 조회한다.
     * 캐시 미스 또는 파싱 실패 시 source만 남긴 빈 결과를 반환한다.
     */
    @Override
    public WeatherData fetchByDistrict(String districtName, OffsetDateTime scheduleTime) {
        String redisKey = weatherMappingKeyPolicy.buildKey(districtName, scheduleTime);
        String cachedBody;
        try {
            cachedBody = stringRedisTemplate.opsForValue().get(redisKey);
        } catch (Exception exception) {
            // Redis 서버 연결 이슈가 있어도 추천 파이프라인은 진행할 수 있도록 캐시 미스로 처리한다.
            log.warn("Redis 날씨 캐시 조회 실패. 캐시 미스로 처리한다. key={}", redisKey, exception);
            return new WeatherData(null, null, null, null, WeatherSourceType.REDIS_CACHE_MISS);
        }

        if (cachedBody == null || cachedBody.isBlank()) {
            return new WeatherData(null, null, null, null, WeatherSourceType.REDIS_CACHE_MISS);
        }

        try {
            RedisWeatherDto cacheData = objectMapper.readValue(cachedBody, RedisWeatherDto.class);
            return new WeatherData(
                cacheData.temperature(),
                cacheData.feelsLike(),
                cacheData.weatherStatus(),
                cacheData.rainProbability(),
                WeatherSourceType.REDIS_CACHE
            );
        } catch (Exception exception) {
            throw RecommendationException.weatherCacheParseFailed(exception);
        }
    }
}


