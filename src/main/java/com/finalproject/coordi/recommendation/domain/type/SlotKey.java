package com.finalproject.coordi.recommendation.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 코디 슬롯 enum.
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
