package com.finalproject.coordi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C100", "입력 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C101", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C102", "서버 내부에서 처리되지 않은 예외가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C103", "데이터베이스 처리 중 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C104", "요청하신 리소스를 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U100", "해당 사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "U101", "이미 가입된 이메일입니다."),
    
    // Item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I100", "해당 아이템을 찾을 수 없습니다."),

    // Recommendation - Common / Validation
    RECOMMENDATION_EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "R100", "추천 외부 API 호출 중 오류가 발생했습니다."),
    RECOMMENDATION_LLM_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "R110", "AI 응답을 해석하지 못했습니다."),
    RECOMMENDATION_BLUEPRINT_INVALID(HttpStatus.BAD_GATEWAY, "R120", "AI blueprint 구조가 유효하지 않습니다."),
    RECOMMENDATION_BLUEPRINT_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "R121", "AI blueprint 응답이 비어 있습니다."),
    RECOMMENDATION_BLUEPRINT_ROOT_MISSING(HttpStatus.BAD_GATEWAY, "R122", "AI blueprint 최상위 키 ai_blueprint가 없습니다."),
    RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH(HttpStatus.BAD_GATEWAY, "R123", "검증된 blueprint 타입이 JsonNode가 아닙니다."),
    RECOMMENDATION_BLUEPRINT_ENUM_INVALID(HttpStatus.BAD_GATEWAY, "R124", "AI blueprint enum 코드가 유효하지 않습니다."),
    RECOMMENDATION_BLUEPRINT_REQUIRED_SLOTS_MISSING(HttpStatus.BAD_REQUEST, "R125", "blueprint 필수 슬롯이 누락되었습니다."),
    RECOMMENDATION_VALIDATION_NATURAL_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "R130", "요청 사항을 입력해주세요."),
    RECOMMENDATION_VALIDATION_LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "R131", "위치 정보(위도/경도)가 필요합니다."),
    RECOMMENDATION_PROMPT_TEMPLATE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R140", "프롬프트 템플릿을 읽지 못했습니다."),
    RECOMMENDATION_GEMINI_OUTPUT_SCHEMA_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R141", "Gemini outputSchema 생성에 실패했습니다."),

    // Recommendation - Weather
    RECOMMENDATION_WEATHER_CACHE_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "R200", "날씨 캐시 데이터를 해석하지 못했습니다."),

    // Recommendation - Gemini
    RECOMMENDATION_GEMINI_IMAGE_PROCESS_FAILED(HttpStatus.BAD_GATEWAY, "R300", "Gemini가 업로드 이미지를 처리하지 못했습니다. 실제 JPG/PNG 이미지로 다시 시도해주세요."),
    RECOMMENDATION_GEMINI_API_KEY_INVALID(HttpStatus.BAD_GATEWAY, "R301", "Gemini API 키가 올바르지 않습니다."),
    RECOMMENDATION_GEMINI_MODEL_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "R302", "현재 설정한 Gemini 모델을 사용할 수 없습니다."),
    RECOMMENDATION_GEMINI_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "R303", "Gemini API 호출에 실패했습니다."),
    RECOMMENDATION_GEMINI_API_CALL_UNEXPECTED(HttpStatus.BAD_GATEWAY, "R304", "Gemini API 호출 중 예외가 발생했습니다."),
    RECOMMENDATION_GEMINI_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "R305", "Gemini 응답 본문이 비어 있습니다."),
    RECOMMENDATION_GEMINI_BLUEPRINT_TEXT_MISSING(HttpStatus.BAD_GATEWAY, "R306", "Gemini 응답에서 blueprint text를 찾지 못했습니다."),
    RECOMMENDATION_GEMINI_BLUEPRINT_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "R307", "Gemini blueprint JSON 파싱에 실패했습니다."),
    RECOMMENDATION_GEMINI_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "R308", "Gemini API 클라이언트 요청 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "R309", "Gemini API 서버 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_IO_ERROR(HttpStatus.BAD_GATEWAY, "R310", "Gemini API 통신 중 네트워크 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_API_ERROR(HttpStatus.BAD_GATEWAY, "R311", "Gemini API 오류가 발생했습니다."),
    RECOMMENDATION_GEMINI_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "R312", "Gemini 요청 파라미터가 유효하지 않습니다."),
    RECOMMENDATION_GEMINI_PERMISSION_DENIED(HttpStatus.BAD_GATEWAY, "R313", "Gemini API 접근 권한이 없습니다."),
    RECOMMENDATION_GEMINI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "R314", "Gemini API 호출 한도를 초과했습니다."),
    RECOMMENDATION_GEMINI_ENDPOINT_MISCONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "R315", "Gemini endpoint 설정이 잘못되었습니다. base URL만 설정하고 버전 경로(/v1, /v1beta)는 포함하지 마세요."),

    // Recommendation - Naver Shopping
    RECOMMENDATION_NAVER_SHOPPING_CONFIG_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "R400", "Naver Shopping 설정이 누락되었습니다."),
    RECOMMENDATION_NAVER_SHOPPING_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "R401", "Naver Shopping API 호출에 실패했습니다."),
    RECOMMENDATION_NAVER_SHOPPING_API_CALL_UNEXPECTED(HttpStatus.BAD_GATEWAY, "R402", "Naver Shopping API 호출 중 예외가 발생했습니다."),

    // 💡 Closet
    CLOSET_NOT_FOUND(HttpStatus.NOT_FOUND, "M100", "옷장 정보를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "M101", "이미지 업로드 중 오류가 발생했습니다."),

    // 💡 Admin
    ADMIN_AUTH_FAIL(HttpStatus.FORBIDDEN, "A100", "관리자 권한이 없습니다."),

    // Auth
    AUTH_FAILED(HttpStatus.UNAUTHORIZED, "T100", "인증에 실패하였습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T101", "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "T102", "유효하지 않은 토큰입니다.");

    private final HttpStatusCode status;
    private final String code;
    private final String message;
}