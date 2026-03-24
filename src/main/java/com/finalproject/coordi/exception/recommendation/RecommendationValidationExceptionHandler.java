package com.finalproject.coordi.exception.recommendation;

import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.recommendation.controller.RecommendationController;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * recommendation 입력 Bean Validation 예외를 recommendation 비즈니스 예외로 변환한다.
 */
@ControllerAdvice(assignableTypes = RecommendationController.class)
public class RecommendationValidationExceptionHandler {

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        ConstraintViolationException.class
    })
    public void handleValidationException(Exception exception) {
        // recommendation 입력 계약 위반은 전용 에러 코드로 통일한다.
        throw new RecommendationException.ValidationException(
            ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT,
            exception
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        RecommendationException.ValidationException validationException = findValidationException(exception);
        if (validationException != null) {
            throw validationException;
        }

        // JSON 역직렬화/바인딩 실패는 recommendation 입력 계약 위반으로 처리한다.
        throw new RecommendationException.ValidationException(
            ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT,
            exception
        );
    }

    private RecommendationException.ValidationException findValidationException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RecommendationException.ValidationException validationException) {
                return validationException;
            }
            current = current.getCause();
        }
        return null;
    }
}
