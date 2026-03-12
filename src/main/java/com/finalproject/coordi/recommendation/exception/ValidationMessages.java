package com.finalproject.coordi.recommendation.exception;

/**
 * Bean Validation 메시지 상수를 관리한다.
 */
public final class ValidationMessages {
    public static final String LOCATION_REQUIRED = "위치 정보(위도/경도)가 필요합니다.";
    public static final String NATURAL_TEXT_REQUIRED = "요청 사항을 입력해주세요.";
    public static final String BLUEPRINT_REQUIRED_SLOTS_MISSING = "blueprint 필수 슬롯이 누락되었습니다.";

    private ValidationMessages() {
    }
}


