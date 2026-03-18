package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * recommendation 축소에 따른 파이프라인 분기.
 */
public enum PipelineMode implements CodedEnum {
    LEGACY_FULL("legacy_full"),
    FAST_TOP1("fast_top1");

    private final String code;

    PipelineMode(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }

    @JsonCreator
    public static PipelineMode fromCode(String rawCode) {
        return EnumResolver.fromCode(PipelineMode.class, rawCode);
    }
}
