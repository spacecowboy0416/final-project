package com.finalproject.coordi.recommendation.exception;

import org.springframework.http.HttpStatus;

/**
 * 추천 모듈 공통 에러 코드(임시).
 */
public enum ErrorCodeEnums {
    INVALID_REQUEST("INVALID_REQUEST", "요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_LOCATION("INVALID_LOCATION", "위치 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TIME("INVALID_TIME", "시간 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    WEATHER_FETCH_FAILED("WEATHER_FETCH_FAILED", "날씨 정보를 가져오지 못했습니다.", HttpStatus.BAD_GATEWAY),
    LLM_PARSE_FAILED("LLM_PARSE_FAILED", "AI 응답을 해석하지 못했습니다.", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    DB_ERROR("DB_ERROR", "데이터 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeEnums(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}


