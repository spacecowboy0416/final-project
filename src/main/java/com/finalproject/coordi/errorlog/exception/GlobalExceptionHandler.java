package com.finalproject.coordi.errorlog.exception;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.finalproject.coordi.errorlog.service.AiErrorTrackerService;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AiErrorTrackerService aiErrorTrackerService;

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception e, Model model, HttpServletRequest request) {
        
        // 1. Sentry - 에러 발생 알림 실시간 전송 (변경예정)
        String rawPath = request.getRequestURI();
        String formattedPath = rawPath.replaceAll("/\\d+(?=/|$)", "/{id}");
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String sentryIssueName = String.format("[%d Error] %s", statusCode, formattedPath);

        Sentry.configureScope(scope -> {
            scope.setTransaction(sentryIssueName);
            scope.setTag("api_path", formattedPath);
            scope.setTag("status_code", String.valueOf(statusCode));
            scope.setTag("project_type", "BackEnd Error");
            scope.setFingerprint(List.of(String.valueOf(statusCode), formattedPath, e.getClass().getSimpleName()));
        });
        Sentry.captureException(e);

        // 2. AI 분석 시스템 - SQL 추출 및 AI 전송
        String errorMessage = e.getMessage();
        String failedSqlInfo = "";

        if (e instanceof DataAccessException || e.getClass().getName().contains("MyBatis")) {
            Throwable cause = e.getCause();
            if (cause != null) {
                failedSqlInfo = "\n\n[실패한 DB 쿼리 정보]\n" + cause.getMessage();
            }
        }

        String finalLogMessage = errorMessage + failedSqlInfo;
        aiErrorTrackerService.trackAndAnalyze(e, finalLogMessage);
        log.error("시스템 에러 발생 (Sentry 전송 및 AI 분석 요청 완료): {}", sentryIssueName, e);

        // 3. 사용자 화면
        model.addAttribute("errorMsg", "서버 처리 중 문제가 발생했습니다. 관리자에게 문의하세요.");
        return "error/500";
    }
}