package com.finalproject.coordi.errorlog.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.finalproject.coordi.errorlog.mapper.ErrorLogMapper;
import com.finalproject.coordi.errorlog.dto.SystemErrorLog;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiErrorTrackerService {

    private final ErrorLogMapper errorLogMapper;
    private final GeminiApiService geminiApiService;

    // 기존 파라미터 1개 유지
    @Async
    public void trackAndAnalyze(Exception e) {
        trackAndAnalyze(e, e.getMessage(), null); 
    }

    // 파라미터 2개 유지
    @Async
    public void trackAndAnalyze(Exception e, String customMessage) {
        trackAndAnalyze(e, customMessage, null);
    }

    // 유저 ID를 포함한 메인 분석 및 추적
    @Async
    public void trackAndAnalyze(Exception e, String customMessage, Long userId) {
        try {
            String errorType = e.getClass().getSimpleName();
            String stackTrace = e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "Unknown Source";
            String messageToSave = (customMessage != null) ? customMessage : "No message"; 

            String errorHash = generateHash(errorType + stackTrace);
            SystemErrorLog existingLog = errorLogMapper.findByHash(errorHash);

            if (existingLog != null) {
                errorLogMapper.incrementOccurrence(errorHash);
                log.info("기존 에러 누적 - 타입: {}, 유저ID: {}", errorType, userId);
            } else {
                log.info("신규 에러 발생, AI 분석 시작 - 타입: {}", errorType);
                String aiSolution = geminiApiService.askGemini(messageToSave, stackTrace);

                SystemErrorLog newLog = SystemErrorLog.builder()
                        .errorHash(errorHash)
                        .errorType(errorType)
                        .message(messageToSave)
                        .stackTrace(stackTrace)
                        .aiSolution(aiSolution)
                        .userId(userId) // 유저 ID 할당
                        .build();

                errorLogMapper.insertLog(newLog);
                log.info("신규 에러 로깅 완료 - 유저ID: {}", userId);
            }

        } catch (Exception ex) {
            log.error("AI 에러 트래커 동작 중 오류 발생", ex);
            Sentry.captureException(ex);
        }
    }

    // 에러 고유 해시 생성
    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}