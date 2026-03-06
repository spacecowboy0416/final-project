package com.finalproject.coordi.recommendation.service.outboundadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.dto.CoordinationRequest;
import com.finalproject.coordi.recommendation.exception.AppException;
import com.finalproject.coordi.recommendation.exception.ErrorCode;
import com.finalproject.coordi.recommendation.service.outboundport.AiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * CoordinationRequest를 Gemini API로 전달해 ai_blueprint JSON을 받는 어댑터.
 */
@Component
@RequiredArgsConstructor
public class GeminiAiAdapter implements AiPort {
    private static final String GEMINI_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.api.gemini.key:}")
    private String geminiApiKey;

    /**
     * 입력 계약을 프롬프트와 함께 전송하고 Gemini JSON 결과를 반환한다.
     */
    @Override
    public JsonNode generateCoordination(CoordinationRequest request) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new AppException(ErrorCode.EXTERNAL_API_ERROR, "Gemini API key가 비어 있습니다.");
        }

        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", buildPrompt(request));
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);
        body.set("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ObjectNode> entity = new HttpEntity<>(body, headers);

        JsonNode response = restTemplate.postForObject(
            GEMINI_ENDPOINT + geminiApiKey,
            entity,
            JsonNode.class
        );
        JsonNode textNode = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "Gemini 응답 본문이 비어 있습니다.");
        }
        return parseJsonText(textNode.asText());
    }

    private JsonNode parseJsonText(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "Gemini 응답에서 JSON 객체를 찾지 못했습니다.");
        }
        try {
            return objectMapper.readTree(cleaned.substring(start, end + 1));
        } catch (Exception e) {
            throw new AppException(ErrorCode.LLM_PARSE_FAILED, "Gemini JSON 파싱에 실패했습니다.", e);
        }
    }

    private String buildPrompt(CoordinationRequest request) {
        return """
            Return ONLY valid JSON. No markdown, no explanation.
            Use the exact top-level key: ai_blueprint.
            Required structure:
            {
              "ai_blueprint": {
                "schemaVersion": "1.0.0",
                "main_item_analysis": {"temp":"","season":"","color":"","type":"","style":""},
                "coordination": {
                  "tops|bottoms|outerwear|shoes|accessories": {
                    "item_name":"",
                    "search_query":"",
                    "category":"",
                    "attributes":{"color":"","material":"","fit":"","style":""},
                    "temp_range":[0,0],
                    "reasoning":"",
                    "priority":"essential|optional"
                  }
                },
                "styling_rule_applied":""
              }
            }
            Input:
            - naturalText: %s
            - scheduleTime: %s
            - tpo: %s
            - mood: %s
            - preferences: %s
            - weather.temperature: %s
            - weather.feelsLike: %s
            - weather.status: %s
            - weather.isRaining: %s
            - weather.rainProbability: %s
            - location: (%s, %s) %s
            - photo.mimeType: %s
            - photo.imageBytes(base64): provided
            """.formatted(
            request.naturalText(),
            request.scheduleTime(),
            request.styleContext().tpo(),
            request.styleContext().mood(),
            request.styleContext().preferences(),
            request.weatherInfo().temperature(),
            request.weatherInfo().feelsLike(),
            request.weatherInfo().weatherStatus(),
            request.weatherInfo().isRaining(),
            request.weatherInfo().rainProbability(),
            request.location().latitude(),
            request.location().longitude(),
            request.location().placeName(),
            request.photo().mimeType()
        );
    }
}