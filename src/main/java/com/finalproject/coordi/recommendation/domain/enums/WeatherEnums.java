package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 날씨 API 컨텍스트(계절/강수/날씨상태) 전용 enum 모음.
 */
public final class WeatherEnums {
    // 유틸성 클래스 인스턴스화를 방지한다.
    private WeatherEnums() {
    }

    // 계절 분류 코드
    public enum SeasonType {
        SPRING("spring"),
        SUMMER("summer"),
        FALL("fall"),
        WINTER("winter");

        private final String code;

        SeasonType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static SeasonType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (SeasonType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown SeasonType code: " + rawCode);
        }
    }

    // 강수 형태 분류 코드
    public enum PrecipitationType {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        private final String code;

        PrecipitationType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static PrecipitationType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (PrecipitationType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown PrecipitationType code: " + rawCode);
        }
    }

    // 날씨 상태 분류 코드
    public enum WeatherStatusType {
        CLEAR("clear"),
        CLOUDY("cloudy"),
        RAINY("rainy"),
        SNOWY("snowy"),
        WINDY("windy");

        private final String code;

        WeatherStatusType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static WeatherStatusType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (WeatherStatusType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown WeatherStatusType code: " + rawCode);
        }
    }

    // 강수 확률 구간 분류 코드
    public enum RainProbabilityType {
        VERY_LOW("very_low"),
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        VERY_HIGH("very_high");

        private final String code;

        RainProbabilityType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static RainProbabilityType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (RainProbabilityType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown RainProbabilityType code: " + rawCode);
        }
    }
}
