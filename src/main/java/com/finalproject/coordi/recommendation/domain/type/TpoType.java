package com.finalproject.coordi.recommendation.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * TPO 분류 enum.
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
