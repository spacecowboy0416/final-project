package com.finalproject.coordi.recommendation.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 아이템 우선순위 enum.
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
}
