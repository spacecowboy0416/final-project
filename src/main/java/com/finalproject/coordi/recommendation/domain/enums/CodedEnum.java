package com.finalproject.coordi.recommendation.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 고유 코드값을 가지는 열거형을 위한 공통 인터페이스.
 * JSON 직렬화 시 {@link #code()} 반환값을 사용한다.
 */
public interface CodedEnum {
    @JsonValue
    String code();
}