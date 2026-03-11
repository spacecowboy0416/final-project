package com.finalproject.coordi.recommendation.exception;

import com.finalproject.coordi.recommendation.controller.RecommendationController;
import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
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
    protected ResponseEntity<RecommendationErrorResponse> handleIllegalStateException(
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
            .body(RecommendationErrorResponse.of("R001", e, request));
    }

    /**
     * recommendation 파이프라인의 enum 코드 해석 실패 등 입력/응답 포맷 오류를
     * recommendation 전용 코드로 반환한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<RecommendationErrorResponse> handleIllegalArgumentException(
        final IllegalArgumentException e,
        final HttpServletRequest request
    ) {
        log.error("recommendationIllegalArgumentException: {}", e.getMessage(), e);

        Sentry.withScope(scope -> {
            scope.setTag("api_path", request.getRequestURI());
            scope.setTag("status_code", String.valueOf(HttpStatus.BAD_GATEWAY.value()));
            scope.setTag("project_type", "Recommendation IllegalArgument");
            scope.setLevel(io.sentry.SentryLevel.ERROR);
            scope.setFingerprint(List.of("recommendation_illegal_argument", request.getMethod(), request.getRequestURI()));
            Sentry.captureException(e);
        });

        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(RecommendationErrorResponse.of("R001", e, request));
    }

    /**
     * recommendation 요청에서 DB 연결/트랜잭션 시작 실패가 발생하면
     * datasource 원인 메시지를 recommendation 전용 응답으로 반환한다.
     */
    @ExceptionHandler(CannotCreateTransactionException.class)
    protected ResponseEntity<RecommendationErrorResponse> handleCannotCreateTransactionException(
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
            .body(RecommendationErrorResponse.of("R002", e, request));
    }

    /**
     * recommendation 테스트 페이지에서 백엔드 원인 확인을 쉽게 하기 위한 전용 에러 응답.
     */
    public record RecommendationErrorResponse(
        String code,
        String message,
        String causeClass,
        String causeMessage,
        String path,
        String timestamp
    ) {
        public static RecommendationErrorResponse of(
            final String code,
            final Throwable throwable,
            final HttpServletRequest request
        ) {
            final Throwable root = rootCauseOf(throwable);
            final String message = throwable == null ? null : throwable.getMessage();
            final String fallbackMessage = root == null ? null : root.getMessage();
            return new RecommendationErrorResponse(
                code,
                message == null || message.isBlank() ? fallbackMessage : message,
                root == null ? null : root.getClass().getSimpleName(),
                root == null ? null : root.getMessage(),
                request == null ? null : request.getRequestURI(),
                OffsetDateTime.now().toString()
            );
        }

        private static Throwable rootCauseOf(final Throwable throwable) {
            if (throwable == null) {
                return null;
            }
            Throwable current = throwable;
            while (current.getCause() != null && current.getCause() != current) {
                current = current.getCause();
            }
            return current;
        }
    }
}
