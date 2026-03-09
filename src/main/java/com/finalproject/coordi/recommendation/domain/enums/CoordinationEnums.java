package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 코디 도메인(TPO/스타일/의류속성) 전용 enum 모음.
 */
public final class CoordinationEnums {
    // 유틸성 클래스 인스턴스화를 방지한다.
    private CoordinationEnums() {
    }

    // 약속/출근/캐주얼 등 상황(TPO) 분류 코드
    public enum TpoType {
        DATE("date"),
        WORK("work"),
        CASUAL("casual"),
        EXERCISE("exercise"),
        TRAVEL("travel"),
        FORMAL("formal"),
        FUNERAL("funeral"),
        WEDDING("wedding");

        private final String code;

        TpoType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static TpoType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (TpoType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown TpoType code: " + rawCode);
        }
    }

    // 코디 무드/스타일 분류 코드
    public enum StyleType {
        MINIMAL("minimal"),
        COMFORTABLE("comfortable"),
        STREET("street"),
        SPORTY("sporty"),
        CLASSIC("classic"),
        GLAM("glam");

        private final String code;

        StyleType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static StyleType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (StyleType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown StyleType code: " + rawCode);
        }
    }

    // 주요 색상 분류 코드
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

        @JsonCreator
        public static ColorType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (ColorType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown ColorType code: " + rawCode);
        }
    }

    // 의류 소재 분류 코드
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

        @JsonCreator
        public static MaterialType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (MaterialType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown MaterialType code: " + rawCode);
        }
    }

    // 의류 핏 분류 코드
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

        @JsonCreator
        public static FitType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (FitType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown FitType code: " + rawCode);
        }
    }

    // 실제 아이템 카테고리 분류 코드(검색/매핑용)
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

        @JsonCreator
        public static ItemCategoryType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (ItemCategoryType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown ItemCategoryType code: " + rawCode);
        }
    }

    // 코디 슬롯 키 분류 코드(tops/bottoms/outerwear/shoes/accessories)
    public enum CategoryType {
        TOPS("tops"),
        BOTTOMS("bottoms"),
        OUTERWEAR("outerwear"),
        SHOES("shoes"),
        ACCESSORIES("accessories");

        private final String code;

        CategoryType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        @JsonCreator
        public static CategoryType fromCode(String rawCode) {
            if (rawCode == null) {
                return null;
            }
            for (CategoryType value : values()) {
                if (value.code.equalsIgnoreCase(rawCode)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown CategoryType code: " + rawCode);
        }
    }
}

