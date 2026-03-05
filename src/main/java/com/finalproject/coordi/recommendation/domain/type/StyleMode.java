package com.finalproject.coordi.recommendation.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 스타일 모드 enum.
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
