package com.finalproject.coordi.exception;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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

/**
 * 애플리케이션 전역에서 발생하는 예외를 인터셉트하여 일괄 처리하고,
 * 사용자 식별자(user_id)를 포함하여 Sentry 및 AI 에러 트래커에 로그를 기록하는 공통 설정 클래스
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AiErrorTrackerService aiErrorTrackerService;

    /**
     * 현재 SecurityContext에서 인증된 사용자의 식별자(ID)를 안전하게 추출
     * 비로그인 상태이거나 예외가 발생할 경우 null을 반환하여 에러 기록 프로세스가 중단되지 않도록 보호
     */
    private Long getCurrentUserIdSafe() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                User user = (User) authentication.getPrincipal();
                return Long.parseLong(user.getUsername());
            }
        } catch (Exception e) {
            log.warn("에러 로그 기록 중 사용자 ID 추출에 실패했습니다.", e);
        }
        return null;
    }

    /**
     * 단순 정적 자원 누락 에러는 무시 및 Sentry 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        String resourcePath = e.getResourcePath();
        boolean isStaticResource = resourcePath.contains("/css/") || resourcePath.contains("/js/") || resourcePath.contains("/images/");

        if (isStaticResource) {
            log.warn("정적 리소스 누락: {}", resourcePath);
        } else {
            log.warn("404 Not Found: {}", resourcePath);
            
            Long userId = getCurrentUserIdSafe();
            
            // 일반 페이지나 API 경로의 404는 Sentry로 전송하여 추적
            Sentry.withScope(scope -> {
                scope.setLevel(SentryLevel.WARNING);
                scope.setTag("api_path", request.getRequestURI());
                if (userId != null) {
                    scope.setTag("user_id", String.valueOf(userId));
                }
                scope.setFingerprint(List.of("404-not-found", request.getRequestURI()));
                Sentry.captureException(e);
            });
        }
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
        Long userId = getCurrentUserIdSafe();

        Sentry.withScope(scope -> {
            scope.setTag("api_path", request.getRequestURI());
            scope.setTag("status_code", String.valueOf(status.value()));
            scope.setTag("error_code", errorCode.getCode());
            if (userId != null) {
                scope.setTag("user_id", String.valueOf(userId));
            }
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

        // 로그인 미인증 사용자 접근 시 메인 페이지 리다이렉트 제어
        if (e.getMessage() != null && e.getMessage().contains("로그인이 필요한 서비스입니다")) {
            return "redirect:/?loginRequired=true";
        }

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String formattedPath = request.getRequestURI().replaceAll("/\\d+(?=/|$)", "/{id}");
        String transactionName = String.format("[%s] %s", request.getMethod(), formattedPath); 
        
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
        Long userId = getCurrentUserIdSafe();

        // AI에게 트랜잭션 정보 및 사용자 식별자 전달 로직
        String finalLogMessage = String.format("Transaction: %s\nMessage: %s %s", transactionName, e.getMessage(), failedSqlInfo);
        aiErrorTrackerService.trackAndAnalyze(e, finalLogMessage, userId);

        // 상세 Sentry 기록 제어
        Sentry.withScope(scope -> {
            scope.setTransaction(transactionName);
            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("api_path", formattedPath);
            scope.setTag("project_type", "BackEnd Error");
            scope.setTag("status_code", String.valueOf(status.value()));
            if (userId != null) {
                scope.setTag("user_id", String.valueOf(userId));
            }
            scope.setFingerprint(List.of("500", formattedPath, e.getClass().getSimpleName()));
            Sentry.captureException(e);
        });

        // 클라이언트 응답 분기 처리
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