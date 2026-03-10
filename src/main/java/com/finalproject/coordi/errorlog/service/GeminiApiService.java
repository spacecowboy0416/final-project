package com.finalproject.coordi.errorlog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GeminiApiService {

    @Value("${external.api.gemini.key}")
    private String apiKey;

    @SuppressWarnings("unchecked") // 형변환 경고 무시
    public String askGemini(String message, String stackTrace) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-latest:generateContent?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();

        String prompt = "스프링 부트 서버에서 에러가 발생했습니다. 신입 개발자가 이해하기 쉽게 원인과 해결 코드를 한국어로 짧고 명확하게 요약해 주세요.\n"
                + "[메시지] " + message + "\n"
                + "[위치] " + stackTrace;

        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{ Map.of("text", prompt) })
            }
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}