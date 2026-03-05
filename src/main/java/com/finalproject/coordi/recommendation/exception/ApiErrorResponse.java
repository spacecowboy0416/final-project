package com.finalproject.coordi.recommendation.exception;

import java.time.OffsetDateTime;

/**
 * 공통 API 에러 응답 모델.
 */
public record ApiErrorResponse(
    OffsetDateTime timestamp,
    ErrorCode code,
    String message,
    String path
) {
}
