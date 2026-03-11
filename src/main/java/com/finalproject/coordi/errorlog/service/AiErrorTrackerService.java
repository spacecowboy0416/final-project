package com.finalproject.coordi.errorlog.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.finalproject.coordi.errorlog.mapper.ErrorLogMapper;
import com.finalproject.coordi.errorlog.dto.SystemErrorLog; // DTO 임포트 추가

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

    // 기존 파라미터 1개짜리 메서드
    @Async
    public void trackAndAnalyze(Exception e) {
        trackAndAnalyze(e, e.getMessage()); 
    }

    // 파라미터 2개짜리 메인 로직
    @Async
    public void trackAndAnalyze(Exception e, String customMessage) {
        try {
            // 변수 추출
            String errorType = e.getClass().getSimpleName();
            String stackTrace = e.getStackTrace()[0].toString();
            String messageToSave = (customMessage != null) ? customMessage : "No message"; 

            // 에러 식별용 고유 해시값 생성
            String errorHash = generateHash(errorType + stackTrace);

            // 매퍼 메서드(findByHash) 사용
            SystemErrorLog existingLog = errorLogMapper.findByHash(errorHash);

            if (existingLog != null) {
                // 이미 분석된 에러라면 발생 횟수만 1 증가
                errorLogMapper.incrementOccurrence(errorHash);
                log.info("기존 에러 누적 (AI 호출 생략) - 타입: {}, 누적 횟수 증가", errorType);
            } else {
                // 처음 보는 에러라면 Gemini AI에게 원인 분석 요청
                log.info("신규 에러 발생, AI 분석 시작 - 타입: {}", errorType);
                String aiSolution = geminiApiService.askGemini(messageToSave, stackTrace);

                // DTO를 생성해서 insertLog에 전달
                SystemErrorLog newLog = new SystemErrorLog();
                newLog.setErrorHash(errorHash);
                newLog.setErrorType(errorType);
                newLog.setMessage(messageToSave);
                newLog.setStackTrace(stackTrace);
                newLog.setAiSolution(aiSolution);

                errorLogMapper.insertLog(newLog);
                log.info("신규 에러 로깅 및 AI 분석 완료 - 타입: {}", errorType);
            }

        } catch (Exception ex) {
            log.error("AI 에러 트래커 자체 동작 중 오류 발생", ex);
        }
    }

    // 3. 에러 타입과 발생 위치를 조합해 고유한 해시(Hash) 값을 만드는 로직
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