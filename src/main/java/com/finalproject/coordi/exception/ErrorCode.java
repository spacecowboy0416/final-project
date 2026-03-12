package com.finalproject.coordi.exception;

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
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "데이터베이스 처리 중 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청하신 리소스를 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "U002", "이미 가입된 이메일입니다."),
    
    // Item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "해당 아이템을 찾을 수 없습니다."),

    // 💡 Recommendation
    RECOMMEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "R001", "추천 정보를 불러오는 데 실패했습니다."),

    // 💡 Closet
    CLOSET_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "옷장 정보를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "M002", "이미지 업로드 중 오류가 발생했습니다."),

    // 💡 Admin
    ADMIN_AUTH_FAIL(HttpStatus.FORBIDDEN, "A001", "관리자 권한이 없습니다.");

    private final HttpStatusCode status;
    private final String code;
    private final String message;
}