package com.finalproject.coordi.recommendation.exception;

/**
 * 추천 모듈 공통 에러 코드.
 */
public enum ErrorCode {
    INVALID_REQUEST,
    INVALID_LOCATION,
    INVALID_TIME,
    WEATHER_FETCH_FAILED,
    LLM_PARSE_FAILED,
    EXTERNAL_API_ERROR,
    DB_ERROR,
    INTERNAL_ERROR
}
