package com.finalproject.coordi.recommendation.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 요청 검증 실패를 공통 에러 응답으로 변환한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(
            new ApiErrorResponse(
                OffsetDateTime.now(),
                ErrorCode.INVALID_REQUEST,
                message,
                request.getRequestURI()
            )
        );
    }

    // 모듈 커스텀 예외를 코드별 HTTP 상태로 변환한다.
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(
        AppException ex,
        HttpServletRequest request
    ) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case INVALID_LOCATION -> HttpStatus.BAD_REQUEST;
            case INVALID_TIME -> HttpStatus.BAD_REQUEST;
            case WEATHER_FETCH_FAILED -> HttpStatus.BAD_GATEWAY;
            case LLM_PARSE_FAILED -> HttpStatus.BAD_GATEWAY;
            case EXTERNAL_API_ERROR -> HttpStatus.BAD_GATEWAY;
            case DB_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                OffsetDateTime.now(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
            )
        );
    }

    // 메서드 레벨(@Validated) 검증 실패를 공통 에러 응답으로 변환한다.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .orElse("요청 값이 올바르지 않습니다.");

        return ResponseEntity.badRequest().body(
            new ApiErrorResponse(
                OffsetDateTime.now(),
                ErrorCode.INVALID_REQUEST,
                message,
                request.getRequestURI()
            )
        );
    }

    // 예상하지 못한 예외를 내부 서버 에러로 변환한다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownException(
        Exception ex,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiErrorResponse(
                OffsetDateTime.now(),
                ErrorCode.INTERNAL_ERROR,
                ex.getMessage(),
                request.getRequestURI()
            )
        );
    }
}
