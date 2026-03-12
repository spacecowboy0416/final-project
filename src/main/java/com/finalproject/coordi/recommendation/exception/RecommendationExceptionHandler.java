package com.finalproject.coordi.recommendation.exception;

import com.finalproject.coordi.recommendation.controller.RecommendationController;
import com.finalproject.coordi.exception.GlobalExceptionHandler;
import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = RecommendationController.class)
public class RecommendationExceptionHandler {

    /**
     * recommendation 파이프라인에서 외부 API 호출/파싱 단계의 상태 예외를
     * 클라이언트에서 바로 확인할 수 있게 recommendation 전용 코드로 반환한다.
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<GlobalExceptionHandler.ErrorResponse> handleIllegalStateException(
        final IllegalStateException e,
        final HttpServletRequest request
    ) {
        log.error("recommendationIllegalStateException: {}", e.getMessage(), e);

        Sentry.withScope(scope -> {
            scope.setTag("api_path", request.getRequestURI());
            scope.setTag("status_code", String.valueOf(HttpStatus.BAD_GATEWAY.value()));
            scope.setTag("project_type", "Recommendation IllegalState");
            scope.setLevel(io.sentry.SentryLevel.ERROR);
            scope.setFingerprint(List.of("recommendation_illegal_state", request.getMethod(), request.getRequestURI()));
            Sentry.captureException(e);
        });

        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(new GlobalExceptionHandler.ErrorResponse("R001", e.getMessage()));
    }

    /**
     * recommendation 요청에서 DB 연결/트랜잭션 시작 실패가 발생하면
     * datasource 원인 메시지를 recommendation 전용 응답으로 반환한다.
     */
    @ExceptionHandler(CannotCreateTransactionException.class)
    protected ResponseEntity<GlobalExceptionHandler.ErrorResponse> handleCannotCreateTransactionException(
        final CannotCreateTransactionException e,
        final HttpServletRequest request
    ) {
        log.error("recommendationCannotCreateTransactionException: {}", e.getMessage(), e);

        Sentry.withScope(scope -> {
            scope.setTag("api_path", request.getRequestURI());
            scope.setTag("status_code", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            scope.setTag("project_type", "Recommendation Transaction");
            scope.setLevel(io.sentry.SentryLevel.ERROR);
            scope.setFingerprint(List.of("recommendation_transaction_error", request.getMethod(), request.getRequestURI()));
            Sentry.captureException(e);
        });

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new GlobalExceptionHandler.ErrorResponse(
                "R002",
                e.getRootCause() == null ? e.getMessage() : e.getRootCause().getMessage()
            ));
    }
}
