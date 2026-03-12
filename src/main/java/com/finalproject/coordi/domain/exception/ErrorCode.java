package com.finalproject.coordi.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부에서 처리되지 않은 예외가 발생했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "U002", "이미 가입된 이메일입니다."),
    
    // Item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "해당 아이템을 찾을 수 없습니다."),

    // Recommendation
    RECOMMENDATION_EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "R001", "추천 외부 API 호출 중 오류가 발생했습니다."),
    RECOMMENDATION_LLM_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "R002", "AI 응답을 해석하지 못했습니다."),
    RECOMMENDATION_BLUEPRINT_INVALID(HttpStatus.BAD_GATEWAY, "R003", "AI blueprint 구조가 유효하지 않습니다."),
    RECOMMENDATION_PROMPT_TEMPLATE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R004", "프롬프트 템플릿을 읽지 못했습니다."),
    RECOMMENDATION_WEATHER_CACHE_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "R005", "날씨 캐시 데이터를 해석하지 못했습니다."),
    RECOMMENDATION_GEMINI_IMAGE_PROCESS_FAILED(HttpStatus.BAD_GATEWAY, "R006", "Gemini가 업로드 이미지를 처리하지 못했습니다. 실제 JPG/PNG 이미지로 다시 시도해주세요."),
    RECOMMENDATION_GEMINI_API_KEY_INVALID(HttpStatus.BAD_GATEWAY, "R007", "Gemini API 키가 올바르지 않습니다."),
    RECOMMENDATION_GEMINI_MODEL_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "R008", "현재 설정한 Gemini 모델을 사용할 수 없습니다."),
    RECOMMENDATION_GEMINI_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "R009", "Gemini API 호출에 실패했습니다."),
    RECOMMENDATION_GEMINI_API_CALL_UNEXPECTED(HttpStatus.BAD_GATEWAY, "R010", "Gemini API 호출 중 예외가 발생했습니다."),
    RECOMMENDATION_GEMINI_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "R011", "Gemini 응답 본문이 비어 있습니다."),
    RECOMMENDATION_GEMINI_BLUEPRINT_TEXT_MISSING(HttpStatus.BAD_GATEWAY, "R012", "Gemini 응답에서 blueprint text를 찾지 못했습니다."),
    RECOMMENDATION_GEMINI_BLUEPRINT_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "R013", "Gemini blueprint JSON 파싱에 실패했습니다."),
    RECOMMENDATION_BLUEPRINT_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "R014", "AI blueprint 응답이 비어 있습니다."),
    RECOMMENDATION_BLUEPRINT_ROOT_MISSING(HttpStatus.BAD_GATEWAY, "R015", "AI blueprint 최상위 키 ai_blueprint가 없습니다."),
    RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH(HttpStatus.BAD_GATEWAY, "R016", "검증된 blueprint 타입이 JsonNode가 아닙니다."),
    RECOMMENDATION_BLUEPRINT_ENUM_INVALID(HttpStatus.BAD_GATEWAY, "R017", "AI blueprint enum 코드가 유효하지 않습니다."),
    RECOMMENDATION_GEMINI_OUTPUT_SCHEMA_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R018", "Gemini outputSchema 생성에 실패했습니다."),
    RECOMMENDATION_VALIDATION_NATURAL_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "R019", "요청 사항을 입력해주세요."),
    RECOMMENDATION_VALIDATION_LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "R020", "위치 정보(위도/경도)가 필요합니다."),
    RECOMMENDATION_BLUEPRINT_REQUIRED_SLOTS_MISSING(HttpStatus.BAD_REQUEST, "R021", "blueprint 필수 슬롯이 누락되었습니다."),
    RECOMMENDATION_GEMINI_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "R022", "Gemini API 클라이언트 요청 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "R023", "Gemini API 서버 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_IO_ERROR(HttpStatus.BAD_GATEWAY, "R024", "Gemini API 통신 중 네트워크 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_API_ERROR(HttpStatus.BAD_GATEWAY, "R025", "Gemini API 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "R026", "Gemini 요청 파라미터가 유효하지 않습니다."),
    RECOMMENDATION_GEMINI_PERMISSION_DENIED(HttpStatus.BAD_GATEWAY, "R027", "Gemini API 접근 권한이 없습니다."),
    RECOMMENDATION_GEMINI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "R028", "Gemini API 호출 한도를 초과했습니다."),
    RECOMMENDATION_GEMINI_ENDPOINT_MISCONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "R029", "Gemini endpoint 설정이 잘못되었습니다. base URL만 설정하고 버전 경로(/v1, /v1beta)는 포함하지 마세요.");

    // 필요한 도메인별 에러 코드 추가...

    private final HttpStatusCode status;
    private final String code;
    private final String message;
}
