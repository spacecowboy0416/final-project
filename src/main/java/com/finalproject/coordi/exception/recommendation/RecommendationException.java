package com.finalproject.coordi.exception.recommendation;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import java.io.IOException;
import org.springframework.web.client.RestClientResponseException;

public final class RecommendationException {
    private static final String GEMINI_BLUEPRINT_PARSE_FAILED_LOG_MESSAGE = "Gemini blueprint JSON 파싱 실패: responseBody={}";

    private RecommendationException() {
    }

    public static String geminiBlueprintParseFailedLogMessage() {
        return GEMINI_BLUEPRINT_PARSE_FAILED_LOG_MESSAGE;
    }

    public static AdapterException geminiApiCallFailed(RestClientResponseException exception) {
        String errorBody = exception.getResponseBodyAsString();
        String detailMessage = "RestClientResponseException [status=%s, body=%s]"
            .formatted(exception.getStatusCode(), abbreviate(errorBody));
        if (containsText(errorBody, "Unable to process input image")) {
            return new AdapterException(
                detailMessage,
                ErrorCode.RECOMMENDATION_GEMINI_IMAGE_PROCESS_FAILED,
                exception
            );
        }
        if (containsText(errorBody, "API_KEY_INVALID", "API key not valid")) {
            return new AdapterException(
                detailMessage,
                ErrorCode.RECOMMENDATION_GEMINI_API_KEY_INVALID,
                exception
            );
        }
        if (containsText(errorBody, "no longer available to new users")
            || containsAllText(errorBody, "model", "not found")) {
            return new AdapterException(
                detailMessage,
                ErrorCode.RECOMMENDATION_GEMINI_MODEL_UNAVAILABLE,
                exception
            );
        }

        int status = exception.getStatusCode().value();
        if (status == 401) {
            return new AdapterException(detailMessage, ErrorCode.RECOMMENDATION_GEMINI_API_KEY_INVALID, exception);
        }
        if (status == 403) {
            return new AdapterException(detailMessage, ErrorCode.RECOMMENDATION_GEMINI_PERMISSION_DENIED, exception);
        }
        if (status == 429) {
            return new AdapterException(detailMessage, ErrorCode.RECOMMENDATION_GEMINI_RATE_LIMITED, exception);
        }
        if (status == 400) {
            return new AdapterException(detailMessage, ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT, exception);
        }
        if (status == 404) {
            return new AdapterException(detailMessage, ErrorCode.RECOMMENDATION_GEMINI_ENDPOINT_MISCONFIGURED, exception);
        }

        return new AdapterException(
            detailMessage,
            ErrorCode.RECOMMENDATION_GEMINI_API_CALL_FAILED,
            exception
        );
    }

    public static AdapterException geminiApiCallUnexpected(Exception exception) {
        return new AdapterException(
            "Unexpected exception [%s: %s]".formatted(exception.getClass().getSimpleName(), exception.getMessage()),
            ErrorCode.RECOMMENDATION_GEMINI_API_CALL_UNEXPECTED,
            exception
        );
    }

    public static AdapterException geminiClientError(ClientException exception) {
        return new AdapterException(
            "ClientException [code=%s, status=%s, message=%s]".formatted(
                exception.code(),
                exception.status(),
                exception.message()
            ),
            resolveGeminiClientErrorCode(exception),
            exception
        );
    }

    public static AdapterException geminiServerError(ServerException exception) {
        return new AdapterException(
            "ServerException [code=%s, status=%s, message=%s]".formatted(
                exception.code(),
                exception.status(),
                exception.message()
            ),
            ErrorCode.RECOMMENDATION_GEMINI_SERVER_ERROR,
            exception
        );
    }

    public static AdapterException geminiIoError(GenAiIOException exception) {
        return new AdapterException(
            "GenAiIOException [message=%s]".formatted(exception.getMessage()),
            ErrorCode.RECOMMENDATION_GEMINI_IO_ERROR,
            exception
        );
    }

    public static AdapterException geminiApiError(ApiException exception) {
        return new AdapterException(
            "ApiException [code=%s, status=%s, message=%s]".formatted(
                exception.code(),
                exception.status(),
                exception.message()
            ),
            ErrorCode.RECOMMENDATION_GEMINI_API_ERROR,
            exception
        );
    }

    public static AdapterException llmResponseEmpty() {
        return new AdapterException(
            ErrorCode.RECOMMENDATION_GEMINI_RESPONSE_EMPTY
        );
    }

    public static AdapterException llmBlueprintTextMissing() {
        return new AdapterException(
            ErrorCode.RECOMMENDATION_GEMINI_BLUEPRINT_TEXT_MISSING
        );
    }

    public static AdapterException llmBlueprintParseFailed(Exception exception) {
        return new AdapterException(
            ErrorCode.RECOMMENDATION_GEMINI_BLUEPRINT_PARSE_FAILED,
            exception
        );
    }

    public static ValidationException blueprintResponseEmpty() {
        return new ValidationException(
            ErrorCode.RECOMMENDATION_BLUEPRINT_RESPONSE_EMPTY
        );
    }

    public static ValidationException blueprintRootMissing() {
        return new ValidationException(
            ErrorCode.RECOMMENDATION_BLUEPRINT_ROOT_MISSING
        );
    }

    public static ValidationException blueprintTypeMismatch() {
        return new ValidationException(
            ErrorCode.RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH
        );
    }

    public static ValidationException blueprintEnumInvalid(Exception cause) {
        return new ValidationException(
            ErrorCode.RECOMMENDATION_BLUEPRINT_ENUM_INVALID,
            cause
        );
    }

    public static ComponentException promptTemplateReadFailed(IOException exception) {
        return new ComponentException(ErrorCode.RECOMMENDATION_PROMPT_TEMPLATE_READ_FAILED, exception);
    }

    public static AdapterException weatherCacheParseFailed(Exception exception) {
        return new AdapterException(
            ErrorCode.RECOMMENDATION_WEATHER_CACHE_PARSE_FAILED,
            exception
        );
    }

    public static ComponentException outputSchemaBuildFailed(Exception exception) {
        return new ComponentException(
            ErrorCode.RECOMMENDATION_GEMINI_OUTPUT_SCHEMA_BUILD_FAILED,
            exception
        );
    }

    public static AdapterException geminiEndpointMisconfigured(String endpoint) {
        return new AdapterException(
            "Gemini endpoint misconfigured [endpoint=%s]".formatted(endpoint),
            ErrorCode.RECOMMENDATION_GEMINI_ENDPOINT_MISCONFIGURED
        );
    }

    /**
     * Gemini 어댑터 계층 예외.
     */
    public static class AdapterException extends BusinessException {
        public AdapterException(String message, ErrorCode errorCode, Throwable cause) {
            super(message, errorCode, cause);
        }

        public AdapterException(String message, ErrorCode errorCode) {
            super(message, errorCode);
        }

        public AdapterException(ErrorCode errorCode, Throwable cause) {
            super(errorCode, cause);
        }

        public AdapterException(ErrorCode errorCode) {
            super(errorCode);
        }
    }

    /**
     * Gemini 입력/출력 검증 예외.
     */
    public static class ValidationException extends BusinessException {
        public ValidationException(ErrorCode errorCode) {
            super(errorCode);
        }

        public ValidationException(ErrorCode errorCode, Throwable cause) {
            super(errorCode, cause);
        }
    }

    /**
     * Gemini 컴포넌트 내부 처리 예외.
     */
    public static class ComponentException extends BusinessException {
        public ComponentException(ErrorCode errorCode, Throwable cause) {
            super(errorCode, cause);
        }
    }

    private static ErrorCode resolveGeminiClientErrorCode(ClientException exception) {
        String status = normalizeUpper(exception.status());
        String errorMessage = exception.message();

        if (exception.code() == 401 || status.contains("UNAUTHENTICATED")) {
            return ErrorCode.RECOMMENDATION_GEMINI_API_KEY_INVALID;
        }
        if (exception.code() == 403 || status.contains("PERMISSION_DENIED")) {
            return ErrorCode.RECOMMENDATION_GEMINI_PERMISSION_DENIED;
        }
        if (exception.code() == 429 || status.contains("RESOURCE_EXHAUSTED")) {
            return ErrorCode.RECOMMENDATION_GEMINI_RATE_LIMITED;
        }
        if (exception.code() == 400 || status.contains("INVALID_ARGUMENT")) {
            if (containsText(errorMessage, "Unable to process input image")) {
                return ErrorCode.RECOMMENDATION_GEMINI_IMAGE_PROCESS_FAILED;
            }
            if (containsText(errorMessage, "API_KEY_INVALID", "API key not valid")) {
                return ErrorCode.RECOMMENDATION_GEMINI_API_KEY_INVALID;
            }
            return ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT;
        }
        if ((exception.code() == 404 || status.contains("NOT_FOUND"))
            && (containsText(errorMessage, "no longer available to new users")
            || containsAllText(errorMessage, "model", "not found"))) {
            return ErrorCode.RECOMMENDATION_GEMINI_MODEL_UNAVAILABLE;
        }
        if (exception.code() == 404 || status.contains("NOT_FOUND")) {
            return ErrorCode.RECOMMENDATION_GEMINI_ENDPOINT_MISCONFIGURED;
        }
        if (containsText(errorMessage, "Unable to process input image")) {
            return ErrorCode.RECOMMENDATION_GEMINI_IMAGE_PROCESS_FAILED;
        }
        if (containsText(errorMessage, "API_KEY_INVALID", "API key not valid")) {
            return ErrorCode.RECOMMENDATION_GEMINI_API_KEY_INVALID;
        }
        if (containsText(errorMessage, "no longer available to new users")) {
            return ErrorCode.RECOMMENDATION_GEMINI_MODEL_UNAVAILABLE;
        }
        return ErrorCode.RECOMMENDATION_GEMINI_CLIENT_ERROR;
    }

    private static String normalizeUpper(String text) {
        return text == null ? "" : text.trim().toUpperCase();
    }

    private static boolean containsText(String source, String... tokens) {
        if (source == null || source.isBlank()) {
            return false;
        }
        String normalizedSource = source.toLowerCase();
        for (String token : tokens) {
            if (token != null && !token.isBlank() && normalizedSource.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAllText(String source, String... tokens) {
        if (source == null || source.isBlank()) {
            return false;
        }
        for (String token : tokens) {
            if (token == null || token.isBlank() || !source.toLowerCase().contains(token.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() <= 500) {
            return text;
        }
        return text.substring(0, 500) + "...(truncated)";
    }
}
