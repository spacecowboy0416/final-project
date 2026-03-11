package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
/**
 * 날씨 API 컨텍스트(계절/날씨상태) 전용 enum 모음.
 */
public final class WeatherEnums {
    // 유틸성 클래스 인스턴스화를 방지한다.
    private WeatherEnums() {
    }

    // 계절 분류 코드
    public enum SeasonType implements CodedEnum {
        SPRING("spring"),
        SUMMER("summer"),
        FALL("fall"),
        WINTER("winter");

        private final String code;

        SeasonType(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static SeasonType fromCode(String rawCode) {
            return EnumResolver.fromCode(SeasonType.class, rawCode);
        }
    }

    // 날씨 상태 분류 코드(룰 엔진용 단일 진실 공급원)
    public enum WeatherStatusType implements CodedEnum {
        CLEAR("clear", "맑음"),
        PARTLY_CLOUDY("partly_cloudy", "구름 조금"),
        CLOUDY("cloudy", "흐림"),
        WINDY("windy", "바람 많음"),
        RAIN("rain", "비"),
        CLOUDY_RAIN("cloudy_rain", "흐리고 비"),
        THUNDERSTORM("thunderstorm", "뇌우"),
        THUNDERSTORM_RAIN("thunderstorm_rain", "뇌우와 비"),
        SNOW("snow", "눈"),
        CLOUDY_SNOW("cloudy_snow", "흐리고 눈"),
        SLEET("sleet", "진눈깨비"),
        HAIL("hail", "우박");

        private final String code;
        private final String displayNameKo;

        WeatherStatusType(String code, String displayNameKo) {
            this.code = code;
            this.displayNameKo = displayNameKo;
        }

        @Override
        public String code() {
            return code;
        }

        public String displayNameKo() {
            return displayNameKo;
        }

        @JsonCreator
        public static WeatherStatusType fromCode(String rawCode) {
            return EnumResolver.fromCode(WeatherStatusType.class, rawCode);
        }
    }

    // 강수 확률 구간 분류 코드
    public enum RainProbabilityType implements CodedEnum {
        VERY_LOW("very_low"),
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        VERY_HIGH("very_high");

        private final String code;

        RainProbabilityType(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static RainProbabilityType fromCode(String rawCode) {
            return EnumResolver.fromCode(RainProbabilityType.class, rawCode);
        }
    }

    // 날씨 조회 데이터 출처 코드
    public enum WeatherSourceType implements CodedEnum {
        REDIS_CACHE("redis_cache"),
        REDIS_CACHE_MISS("redis_cache_miss"),
        REDIS_PARSE_ERROR("redis_parse_error");

        private final String code;

        WeatherSourceType(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static WeatherSourceType fromCode(String rawCode) {
            return EnumResolver.fromCode(WeatherSourceType.class, rawCode);
        }
    }
}
