package com.finalproject.coordi.recommendation.service.apiport;

import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.RainProbabilityType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherSourceType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;
import java.time.OffsetDateTime;

/**
 * 행정구역(구/군)과 추천 기준 시각을 기반으로 Redis 캐시 서버에서 날씨 정보를 조회하기 위한 포트.
 */
public interface WeatherPort {
    /**
     * districtName과 scheduleTime에 대응되는 Redis 캐시 날씨 정보를 조회한다.
     */
    WeatherData fetchByDistrict(String districtName, OffsetDateTime scheduleTime);

    record WeatherData(
        Double temperature,
        Double feelsLike,
        WeatherStatusType weatherStatus,
        RainProbabilityType rainProbability,
        WeatherSourceType source
    ) {
    }
}
