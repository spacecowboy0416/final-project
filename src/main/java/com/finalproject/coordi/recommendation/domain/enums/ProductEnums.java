package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * product /조회에 쓰는 코드 enum 모음.
 */
public final class ProductEnums {
    private ProductEnums() {
    }

    /**
     * product.source 컬럼에 저장하는 상품 출처 코드.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ProductSourceType implements CodedEnum {
        NAVER("NAVER");

        private final String code;

        @JsonCreator
        public static ProductSourceType fromCode(String rawCode) {
            return EnumResolver.fromCode(ProductSourceType.class, rawCode);
        }
    }

    /**
     * recommendation slot을 DB category.code로 바꾸는 매핑 코드.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ProductCategoryCode implements CodedEnum {
        HEADWEAR(CategoryType.HEADWEAR, "headwear"),
        TOPS(CategoryType.TOPS, "top"),
        BOTTOMS(CategoryType.BOTTOMS, "pants"),
        OUTERWEAR(CategoryType.OUTERWEAR, "outerwear"),
        SHOES(CategoryType.SHOES, "shoes"),
        ACCESSORIES(CategoryType.ACCESSORIES, "accessory");

        private final CategoryType slotType;
        private final String code;

        public static ProductCategoryCode fromSlotType(CategoryType slotType) {
            if (slotType == null) {
                throw new IllegalArgumentException("slotType must not be null");
            }

            for (ProductCategoryCode value : values()) {
                if (value.slotType == slotType) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown ProductCategoryCode slotType: " + slotType);
        }
    }
}
