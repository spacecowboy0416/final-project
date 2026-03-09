package com.finalproject.coordi.sentry.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice // 프로젝트 내의 모든 Controller에서 발생하는 예외를 처리하는 클래스
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) // 모든 예외를 처리하는 핸들러
    public ResponseEntity<String> handlResponseEntity(Exception e, HttpServletRequest request) {

        // URL 경로 추출
        String rawPath = request.getRequestURI();
        // 숫자(ID)를 {id}로 치환 - 에러 파편화 방지
        String formattedPath = rawPath.replaceAll("/\\d+(?=/|$)", "/{id}");

        // 상태 코드 설정 (500 서버 에러로 default 처리)
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

        // Sentry 이슈 제목 형식
        String sentryIssueName = String.format("[%d Error] =%s", statusCode, formattedPath);

        Sentry.configureScope((scope -> {
            scope.setTransaction(sentryIssueName); // Sentry 제목 변경 - 위에서 format한 형식
            scope.setTag("api_path", formattedPath); // 검색용 태그 추가
            scope.setTag("status_code", String.valueOf(statusCode));
            scope.setTag("project_type", "BackEnd Error");

            // 동일한 API 경로에서 터진 에러는 Sentry에서 1개의 이슈로 묶도록 설정
            // fingerprint 설정 (custom)
            scope.setFingerprint(List.of(String.valueOf(statusCode), formattedPath, e.getClass().getSimpleName()));

        }));

        // 에러를 Sentry로 전송하고 터미널에도 로그 남기기
        Sentry.captureException(e);
        log.error("서버 에러 발생: {}", sentryIssueName, e);

        // 프론트엔드(사용자가 마주하는 화면)에는 에러 원문 대신 정제된 메시지 반환 처리
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 문제로 요청을 처리하지 못했습니다. 잠시후 다시 시도해주세요");
    }
}