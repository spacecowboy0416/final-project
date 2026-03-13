package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 코디 도메인(TPO/스타일/의류속성) 전용 enum 모음.
 */

public final class CoordinationEnums {
    // 유틸성 클래스 인스턴스화를 방지한다.
    private CoordinationEnums() {
    }

    // 약속/출근/캐주얼 등 상황(TPO) 분류 코드
    public enum TpoType implements CodedEnum {
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

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static TpoType fromCode(String rawCode) {
            return EnumResolver.fromCode(TpoType.class, rawCode);
        }
    }

    // 코디 무드/스타일 분류 코드
    public enum StyleType implements CodedEnum {
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

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static StyleType fromCode(String rawCode) {
            return EnumResolver.fromCode(StyleType.class, rawCode);
        }
    }

    // 주요 색상 분류 코드
    public enum ColorType implements CodedEnum {
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

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static ColorType fromCode(String rawCode) {
            return EnumResolver.fromCode(ColorType.class, rawCode);
        }
    }

    // 의류 소재 분류 코드
    public enum MaterialType implements CodedEnum {
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

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static MaterialType fromCode(String rawCode) {
            return EnumResolver.fromCode(MaterialType.class, rawCode);
        }
    }

    // 의류 핏 분류 코드
    public enum FitType implements CodedEnum {
        SLIM("slim"),
        REGULAR("regular"),
        RELAXED("relaxed"),
        OVERSIZED("oversized"),
        WIDE("wide");

        private final String code;

        FitType(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static FitType fromCode(String rawCode) {
            return EnumResolver.fromCode(FitType.class, rawCode);
        }
    }

    // 실제 아이템 카테고리 분류 코드(검색/매핑용)
    public enum ItemCategoryType implements CodedEnum {
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

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static ItemCategoryType fromCode(String rawCode) {
            return EnumResolver.fromCode(ItemCategoryType.class, rawCode);
        }
    }

    // 코디 슬롯 키 분류 코드(tops/bottoms/outerwear/shoes/accessories)
    public enum CategoryType implements CodedEnum {
        TOPS("tops"),
        BOTTOMS("bottoms"),
        OUTERWEAR("outerwear"),
        SHOES("shoes"),
        ACCESSORIES("accessories");

        private final String code;

        CategoryType(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static CategoryType fromCode(String rawCode) {
            return EnumResolver.fromCode(CategoryType.class, rawCode);
        }
    }

    // 슬롯별 필수도 분류 코드
    public enum PriorityType implements CodedEnum {
        ESSENTIAL("essential"),
        OPTIONAL("optional");

        private final String code;

        PriorityType(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @JsonCreator
        public static PriorityType fromCode(String rawCode) {
            return EnumResolver.fromCode(PriorityType.class, rawCode);
        }
    }
}