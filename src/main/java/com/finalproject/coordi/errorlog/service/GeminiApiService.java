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

    @Value("${external.api.gemini.url}")
    private String apiUrl;

    @SuppressWarnings("unchecked")
    public String askGemini(String message, String stackTrace) {
        
        String requestUrl = apiUrl + "?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();

        String prompt = "당신은 백엔드 시스템의 'Sentry 모니터링 분석 AI'입니다. 전달된 에러를 분석하여 Sentry Alert 규격에 맞는 '이슈 요약 리포트'를 작성하세요.\n"
                + "불필요한 설명, 인사말, 마크다운 기호(#, *, ` 등)는 절대 배제하고 오직 아래 3가지 Sentry 태그 포맷으로만 답변하세요.\n\n"
                + "포맷:\n"
                + "[Issue] 에러 클래스명 및 발생 위치\n"
                + "[Root Cause] 기술적 원인 1문장\n"
                + "[Action] 해결을 위한 코드/DB 수정 지시 1문장\n\n"
                + "[Sentry Context]\n" + message + "\n"
                + "StackTrace: " + stackTrace;

        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{ Map.of("text", prompt) })
            }
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(requestUrl, requestBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}