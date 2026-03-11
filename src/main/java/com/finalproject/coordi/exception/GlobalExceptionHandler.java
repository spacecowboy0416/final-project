package com.finalproject.coordi.exception;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.finalproject.coordi.errorlog.service.AiErrorTrackerService;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AiErrorTrackerService aiErrorTrackerService;

    /**
     * 단순 정적 자원 누락 에러는 무시
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException e) {
        log.warn("정적 자원 누락 (AI 분석 무시됨): {}", e.getMessage());
        return ResponseEntity.notFound().build(); 
    }

    /**
     * 직접 정의한 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException e, final HttpServletRequest request) {
        log.warn("BusinessException 발생: {}", e.getMessage());

        final ErrorCode errorCode = e.getErrorCode();
        final HttpStatusCode status = Objects.requireNonNull(errorCode.getStatus());

        Sentry.withScope(scope -> {
            scope.setTag("api_path", request.getRequestURI());
            scope.setTag("status_code", String.valueOf(status.value()));
            scope.setTag("error_code", errorCode.getCode());
            scope.setLevel(SentryLevel.WARNING);
            scope.setFingerprint(List.of(errorCode.getCode(), request.getMethod(), request.getRequestURI()));
            Sentry.captureException(e);
        });

        return new ResponseEntity<>(ErrorResponse.of(errorCode), status);
    }

    /**
     * 처리되지 않은 모든 시스템 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected Object handleException(final Exception e, final HttpServletRequest request, Model model) {
        log.error("Unhandled Exception 발생: {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String formattedPath = request.getRequestURI().replaceAll("/\\d+(?=/|$)", "/{id}");
        String transactionName = String.format("[%s] %s", request.getMethod(), formattedPath); // Sentry 트랜잭션 규격
        
        // 에러의 '진짜 원인(Root Cause)' 찾기
        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String failedSqlInfo = "";
        if (rootCause instanceof java.sql.SQLException || e instanceof DataAccessException || rootCause.getClass().getName().contains("MyBatis")) {
            errorCode = ErrorCode.DATABASE_ERROR;
            failedSqlInfo = "\n[Root Cause] " + rootCause.getMessage();
        }
        
        final HttpStatusCode status = Objects.requireNonNull(errorCode.getStatus());

        // AI에게 Sentry 트랜잭션 정보 넘김
        String finalLogMessage = String.format("Transaction: %s\nMessage: %s %s", transactionName, e.getMessage(), failedSqlInfo);
        aiErrorTrackerService.trackAndAnalyze(e, finalLogMessage);

        // 상세 Sentry 기록
        Sentry.withScope(scope -> {
            scope.setTransaction(transactionName);
            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("api_path", formattedPath);
            scope.setTag("project_type", "BackEnd Error");
            scope.setTag("status_code", String.valueOf(status.value()));
            scope.setFingerprint(List.of("500", formattedPath, e.getClass().getSimpleName()));
            Sentry.captureException(e);
        });

        // 클라이언트 응답 분기
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            model.addAttribute("errorMsg", errorCode.getMessage());
            model.addAttribute("errorCode", errorCode.getCode());
            return "error/500";
        }

        return new ResponseEntity<>(ErrorResponse.of(errorCode), status);
    }

    public record ErrorResponse(String code, String message) {
        public static ErrorResponse of(ErrorCode errorCode) {
            return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        }
    }
}