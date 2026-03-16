package com.finalproject.coordi.recommendation.domain.enums;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;

/**
 * Naver Shopping 검색어 조합 규칙을 구성하는 토큰 타입 정의.
 */
public final class ShoppingQueryEnums {
    private ShoppingQueryEnums() {
    }

    public enum QueryTokenType {
        GENDER,
        COLOR,
        FIT,
        MATERIAL,
        STYLE,
        CATEGORY,
        TPO
    }

    public enum QuerySortType {
        SIM("sim");

        private final String apiValue;

        QuerySortType(String apiValue) {
            this.apiValue = apiValue;
        }

        public String apiValue() {
            return apiValue;
        }
    }

    public enum GenderKeyword {
        MALE(BlueprintInputDto.GenderType.MALE, "남성"),
        FEMALE(BlueprintInputDto.GenderType.FEMALE, "여성"),
        UNISEX(BlueprintInputDto.GenderType.UNISEX, "공용");

        private final BlueprintInputDto.GenderType source;
        private final String keyword;

        GenderKeyword(BlueprintInputDto.GenderType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public String keyword() {
            return keyword;
        }

        public static String from(BlueprintInputDto.GenderType source) {
            if (source == null) {
                return null;
            }
            for (GenderKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }

    public enum ColorKeyword {
        BLACK(ColorType.BLACK, "블랙"),
        WHITE(ColorType.WHITE, "화이트"),
        GRAY(ColorType.GRAY, "그레이"),
        NAVY(ColorType.NAVY, "네이비"),
        BLUE(ColorType.BLUE, "블루"),
        BEIGE(ColorType.BEIGE, "베이지"),
        BROWN(ColorType.BROWN, "브라운"),
        KHAKI(ColorType.KHAKI, "카키"),
        GREEN(ColorType.GREEN, "그린"),
        RED(ColorType.RED, "레드"),
        PINK(ColorType.PINK, "핑크"),
        PURPLE(ColorType.PURPLE, "퍼플"),
        YELLOW(ColorType.YELLOW, "옐로우"),
        ORANGE(ColorType.ORANGE, "오렌지");

        private final ColorType source;
        private final String keyword;

        ColorKeyword(ColorType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public static String from(ColorType source) {
            if (source == null) {
                return null;
            }
            for (ColorKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }

    public enum FitKeyword {
        SLIM(FitType.SLIM, "슬림핏"),
        REGULAR(FitType.REGULAR, "기본핏"),
        RELAXED(FitType.RELAXED, "루즈핏"),
        OVERSIZED(FitType.OVERSIZED, "오버핏"),
        WIDE(FitType.WIDE, "와이드");

        private final FitType source;
        private final String keyword;

        FitKeyword(FitType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public static String from(FitType source) {
            if (source == null) {
                return null;
            }
            for (FitKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }

    public enum MaterialKeyword {
        COTTON(MaterialType.COTTON, "코튼"),
        DENIM(MaterialType.DENIM, "데님"),
        WOOL(MaterialType.WOOL, "울"),
        LEATHER(MaterialType.LEATHER, "레더"),
        KNIT(MaterialType.KNIT, "니트"),
        LINEN(MaterialType.LINEN, "린넨"),
        NYLON(MaterialType.NYLON, "나일론"),
        POLYESTER(MaterialType.POLYESTER, "폴리에스터");

        private final MaterialType source;
        private final String keyword;

        MaterialKeyword(MaterialType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public static String from(MaterialType source) {
            if (source == null) {
                return null;
            }
            for (MaterialKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }

    public enum StyleKeyword {
        MINIMAL(StyleType.MINIMAL, "미니멀"),
        COMFORTABLE(StyleType.COMFORTABLE, "편안한"),
        STREET(StyleType.STREET, "스트릿"),
        SPORTY(StyleType.SPORTY, "스포티"),
        CLASSIC(StyleType.CLASSIC, "클래식"),
        GLAM(StyleType.GLAM, "글램");

        private final StyleType source;
        private final String keyword;

        StyleKeyword(StyleType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public static String from(StyleType source) {
            if (source == null) {
                return null;
            }
            for (StyleKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }

    public enum CategoryKeyword {
        TOP(ItemCategoryType.TOP, "반팔티"),
        PANTS(ItemCategoryType.PANTS, "슬랙스"),
        OUTERWEAR(ItemCategoryType.OUTERWEAR, "아우터"),
        SHOES(ItemCategoryType.SHOES, "스니커즈"),
        ACCESSORY(ItemCategoryType.ACCESSORY, "토트백"),
        DRESS(ItemCategoryType.DRESS, "원피스"),
        JACKET(ItemCategoryType.JACKET, "자켓");

        private final ItemCategoryType source;
        private final String keyword;

        CategoryKeyword(ItemCategoryType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public static String from(ItemCategoryType source) {
            if (source == null) {
                return null;
            }
            for (CategoryKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }

    public enum TpoKeyword {
        DATE(TpoType.DATE, "데이트룩"),
        WORK(TpoType.WORK, "오피스룩"),
        CASUAL(TpoType.CASUAL, "데일리룩"),
        EXERCISE(TpoType.EXERCISE, "운동복"),
        TRAVEL(TpoType.TRAVEL, "여행룩"),
        FORMAL(TpoType.FORMAL, "포멀룩"),
        FUNERAL(TpoType.FUNERAL, "블랙포멀"),
        WEDDING(TpoType.WEDDING, "하객룩");

        private final TpoType source;
        private final String keyword;

        TpoKeyword(TpoType source, String keyword) {
            this.source = source;
            this.keyword = keyword;
        }

        public static String from(TpoType source) {
            if (source == null) {
                return null;
            }
            for (TpoKeyword value : values()) {
                if (value.source == source) {
                    return value.keyword;
                }
            }
            return null;
        }
    }
}
