package com.finalproject.coordi.recommendation.domain;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis에 저장된 구/군 단위 날씨 캐시 키 규칙을 한 곳에서 관리하는 정책 객체.
 * recommendation의 Redis 날씨 조회는 districtName과 30분 슬롯으로 정규화한 scheduleTime을 조합해 키를 만든다.
 */
@Component
public class WeatherMappingKeyPolicy {
    private static final DateTimeFormatter WEATHER_SLOT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @Value("${external.api.weather.redis.key-prefix:weather:district}")
    private String weatherRedisKeyPrefix;

    /**
     * districtName과 scheduleTime을 recommendation 표준 Redis 날씨 키 형식으로 조합한다.
     */
    public String buildKey(String districtName, OffsetDateTime scheduleTime) {
        String normalizedDistrictName = districtName == null ? "" : districtName.trim();
        OffsetDateTime normalizedScheduleTime = normalizeScheduleTime(scheduleTime);
        return weatherRedisKeyPrefix + ":" + normalizedDistrictName + ":" + WEATHER_SLOT_FORMATTER.format(normalizedScheduleTime);
    }

    /**
     * Redis 날씨 캐시가 30분 단위 슬롯으로 저장된다는 전제에 맞춰 scheduleTime을 가장 가까운 30분 경계로 맞춘다.
     */
    public OffsetDateTime normalizeScheduleTime(OffsetDateTime scheduleTime) {
        OffsetDateTime normalized = scheduleTime.withSecond(0).withNano(0);
        int minute = normalized.getMinute();
        if (minute < 15) {
            return normalized.withMinute(0);
        }
        if (minute < 45) {
            return normalized.withMinute(30);
        }
        return normalized.plusHours(1).withMinute(0);
    }
}
