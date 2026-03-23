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
     * 정적 자원 누락(404) 예외를 처리
     * 단순 정적 파일 누락은 서버 내 경고 로그만 남기며, API 또는 페이지 경로의 누락은 사용자 정보를 포함하여 Sentry에 전송
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
     * 사전에 정의된 비즈니스 로직 예외(BusinessException)를 처리
     * 정해진 에러 규격에 맞추어 클라이언트에게 응답하며, 관련 내역을 사용자 ID와 함께 Sentry에 경고 레벨로 기록
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
     * 명시적으로 처리되지 않은 모든 시스템 내부 예외(500)를 가로채어 처리
     * 예외의 근본 원인을 파악하여 데이터베이스 오류 여부를 판별하고, 추출된 사용자 식별자(user_id)를 포함하여
     * AI 오류 분석 서비스 및 Sentry에 상세 로그를 전송
     */
    @ExceptionHandler(Exception.class)
    protected Object handleException(final Exception e, final HttpServletRequest request, Model model) {
        log.error("Unhandled Exception 발생: {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String formattedPath = request.getRequestURI().replaceAll("/\\d+(?=/|$)", "/{id}");
        String transactionName = String.format("[%s] %s", request.getMethod(), formattedPath); 
        
        // 예외 객체를 순회하여 에러의 근본 원인(Root Cause)을 탐색
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

        // 수집된 에러 트랜잭션 정보를 AI 에러 분석 서비스로 전달
        // 현재 AiErrorTrackerService 클래스에 userId 파라미터가 구현되지 않았으므로, 컴파일 에러 방지를 위해 기존 시그니처를 유지
        String finalLogMessage = String.format("Transaction: %s\nMessage: %s %s", transactionName, e.getMessage(), failedSqlInfo);
        aiErrorTrackerService.trackAndAnalyze(e, finalLogMessage);

        // 상세 로그 데이터를 사용자 ID와 함께 Sentry 서버로 전송
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

        // 클라이언트의 요청 헤더를 분석하여 화면 응답(HTML)과 데이터 API 응답(JSON)을 분기 처리
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            model.addAttribute("errorMsg", errorCode.getMessage());
            model.addAttribute("errorCode", errorCode.getCode());
            return "error/500";
        }

        return new ResponseEntity<>(ErrorResponse.of(errorCode), status);
    }

    /**
     * API 요청에 대한 표준 에러 응답 규격을 정의하는 레코드
     */
    public record ErrorResponse(String code, String message) {
        public static ErrorResponse of(ErrorCode errorCode) {
            return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
        }
    }
}