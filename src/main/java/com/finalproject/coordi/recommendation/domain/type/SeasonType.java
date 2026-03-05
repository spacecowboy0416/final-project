package com.finalproject.coordi.recommendation.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 계절 분류 enum.
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
