package com.finalproject.coordi.sentry.exception;

import com.finalproject.coordi.domain.exception.BusinessException;
import com.finalproject.coordi.domain.exception.ErrorCode;
import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 비즈니스 예외 처리
     * (예: 사용자를 찾지 못함, 중복된 이메일 등)
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException e, final HttpServletRequest request) {
        log.warn("handleBusinessException: {}", e.getMessage());

        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode);
        final HttpStatusCode status = Objects.requireNonNull(errorCode.getStatus());

        // BusinessException은 "예상된 예외"이므로 Sentry에서는 경고(Warning) 수준으로 기록합니다.
        Sentry.withScope(scope -> {
            scope.setTag("api_path", request.getRequestURI());
            scope.setTag("status_code", String.valueOf(status.value()));
            scope.setTag("error_code", errorCode.getCode());
            scope.setLevel(io.sentry.SentryLevel.WARNING);
            scope.setFingerprint(List.of(errorCode.getCode(), request.getMethod(), request.getRequestURI())); // 에러코드, 메서드, 경로로 그룹핑
            Sentry.captureException(e);
        });

        return new ResponseEntity<>(response, status);
    }

    /**
     * 처리되지 않은 모든 예외 처리 (최후의 보루)
     * (예: NullPointerException, 기타 런타임 예외 등)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(final Exception e, final HttpServletRequest request) {
        log.error("unhandledException: {}", e.getMessage(), e);

        final ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        final ErrorResponse response = ErrorResponse.of(errorCode);
        final HttpStatusCode status = Objects.requireNonNull(errorCode.getStatus());

        // 처리되지 않은 예외는 "치명적(Error)" 수준으로 Sentry에 기록합니다.
        String formattedPath = request.getRequestURI().replaceAll("/\\d+(?=/|$)", "/{id}");
        Sentry.withScope(scope -> {
            scope.setTransaction(String.format("[%s] %s", request.getMethod(), formattedPath));
            scope.setTag("api_path", formattedPath);
            scope.setTag("status_code", String.valueOf(status.value()));
            scope.setTag("project_type", "BackEnd Error");
            scope.setLevel(io.sentry.SentryLevel.ERROR);
            scope.setFingerprint(List.of("500", formattedPath, e.getClass().getSimpleName())); // 500에러, 경로, 예외 클래스명으로 그룹핑
            Sentry.captureException(e);
        });

        return new ResponseEntity<>(response, status);
    }

    /**
     * 표준 에러 응답 DTO
     *
     * @param code    - 비즈니스 에러 코드 (U001, I001 등)
     * @param message - 사용자에게 보여주기 위한 친절한 메시지
     */
    public record ErrorResponse(String code, String message) {
        public static ErrorResponse of(ErrorCode errorCode) {
            return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        }
    }
}
