package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 추천 컨텍스트(TPO/스타일/계절/강수)에서 공통으로 사용하는 enum들을 한곳에 모아 관리한다.
 */
public final class ContextEnums {
    private ContextEnums() {
    }

    /**
     * 사용자 상황(TPO)을 표준 코드로 정규화한다.
     */
    public enum TpoType {
        DATE("date"),
        WORK("work"),
        CASUAL("casual"),
        EXERCISE("exercise"),
        TRAVEL("travel"),
        FORMAL("formal");

        private final String code;

        TpoType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }

    /**
     * 스타일 무드를 시스템 표준 코드로 정규화한다.
     */
    public enum StyleMode {
        MINIMAL("minimal"),
        COMFORTABLE("comfortable"),
        STREET("street"),
        SPORTY("sporty"),
        CLASSIC("classic"),
        GLAM("glam");

        private final String code;

        StyleMode(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }

    /**
     * 날씨/코디 분석에 사용하는 계절 분류다.
     */
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
    }

    /**
     * 기상 데이터의 강수 상태를 나타낸다.
     */
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
    }

    /**
     * 코디 및 아이템 속성에서 사용하는 대표 색상 코드다.
     */
    public enum ColorType {
        BLACK("black"),
        WHITE("white"),
        GRAY("gray"),
        NAVY("navy"),
        BLUE("blue"),
        BEIGE("beige"),
        BROWN("brown"),
        KHAKI("khaki"),
        GREEN("green"),
        RED("red"),
        PINK("pink"),
        PURPLE("purple"),
        YELLOW("yellow"),
        ORANGE("orange");

        private final String code;

        ColorType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }

    /**
     * 의류 소재를 표준 값으로 관리하기 위한 enum.
     */
    public enum MaterialType {
        COTTON("cotton"),
        DENIM("denim"),
        WOOL("wool"),
        LEATHER("leather"),
        KNIT("knit"),
        LINEN("linen"),
        NYLON("nylon"),
        POLYESTER("polyester");

        private final String code;

        MaterialType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }

    /**
     * 실루엣/착용감 관련 핏 표현을 표준화한다.
     */
    public enum FitType {
        SLIM("slim"),
        REGULAR("regular"),
        RELAXED("relaxed"),
        OVERSIZED("oversized"),
        WIDE("wide");

        private final String code;

        FitType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }

    /**
     * 메인 아이템 분석과 슬롯 분류에서 공통으로 재사용 가능한 카테고리 enum.
     */
    public enum ItemCategoryType {
        TOP("top"),
        PANTS("pants"),
        OUTERWEAR("outerwear"),
        SHOES("shoes"),
        ACCESSORY("accessory"),
        DRESS("dress"),
        JACKET("jacket");

        private final String code;

        ItemCategoryType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }

    /**
     * 사용자 입력/기상 API의 날씨 상태를 정규화한다.
     */
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
    }
}
