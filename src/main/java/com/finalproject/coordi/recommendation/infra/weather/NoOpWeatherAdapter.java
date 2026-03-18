package com.finalproject.coordi.recommendation.infra.weather;

import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherSourceType;
import com.finalproject.coordi.recommendation.service.payload.WeatherPort;
import java.time.OffsetDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Redis 캐시를 사용할 수 없는 환경에서 동작하는 대체 날씨 어댑터.
 * recommendation 파이프라인이 Redis 의존성 없이도 진행되도록 캐시 미스를 반환한다.
 */
@Component
@ConditionalOnMissingBean(WeatherPort.class)
public class NoOpWeatherAdapter implements WeatherPort {

    @Override
    public WeatherData fetchByDistrict(String districtName, OffsetDateTime scheduleTime) {
        return new WeatherData(null, null, null, null, WeatherSourceType.REDIS_CACHE_MISS);
    }
}
