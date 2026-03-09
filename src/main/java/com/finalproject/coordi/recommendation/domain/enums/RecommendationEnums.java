package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 추천 실행 상태와 슬롯 메타데이터 등 추천 도메인 전용 enum들을 모아 관리한다.
 */
public final class RecommendationEnums {
    private RecommendationEnums() {
    }

    /**
     * AI blueprint 생성 출처를 나타낸다.
     */
    public enum BlueprintSource {
        GEMINI,
        FALLBACK
    }

    /**
     * 추천 파이프라인의 처리 상태를 나타낸다.
     */
    public enum RecommendationStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

    /**
     * 슬롯 아이템의 필수 여부를 나타낸다.
     */
    public enum PriorityType {
        ESSENTIAL("essential"),
        OPTIONAL("optional");

        private final String code;

        PriorityType(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }

        public static PriorityType fromCode(String rawCode) {
            if (rawCode == null) {
                return ESSENTIAL;
            }
            return OPTIONAL.code.equalsIgnoreCase(rawCode) ? OPTIONAL : ESSENTIAL;
        }
    }

    /**
     * AI blueprint와 내부 모델이 공유하는 슬롯 키다.
     */
    public enum SlotKey {
        TOPS("tops"),
        BOTTOMS("bottoms"),
        OUTERWEAR("outerwear"),
        SHOES("shoes"),
        ACCESSORIES("accessories");

        private final String code;

        SlotKey(String code) {
            this.code = code;
        }

        @JsonValue
        public String code() {
            return code;
        }
    }
}
