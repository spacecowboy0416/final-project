package com.finalproject.coordi.recommendation.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiAdapter implements GeminiPort {
    private static final String GEMINI_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${external.api.gemini.key:}")
    private String geminiApiKey;

    public GeminiAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Gemini API를 호출해 DRAFT ai_blueprint JSON을 생성한다.
    @Override
    public JsonNode generateDraftBlueprint(RecommendationRequest request, GeminiDraftContext context) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is empty.");
        }

        String prompt = buildPrompt(request, context);
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", prompt);
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
            throw new IllegalStateException("Gemini response is empty.");
        }

        String extracted = extractJson(textNode.asText());
        try {
            return objectMapper.readTree(extracted);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini JSON parse failed: " + e.getMessage(), e);
        }
    }

    // LLM 출력 계약을 강제하는 프롬프트를 구성한다.
    private String buildPrompt(RecommendationRequest request, GeminiDraftContext context) {
        List<String> tags = request.userTags() == null ? List.of() : request.userTags();
        return """
            Return ONLY valid JSON, no markdown, no explanation.
            Build ai_blueprint with schemaVersion 1.0.0 and coordination slots:
            tops, bottoms, outerwear, shoes, accessories.
            Each slot must include:
            item_name, search_query, category, attributes(color, material, fit, style), temp_range, reasoning, priority.
            Input:
            - inputText: %s
            - imageUrl: %s
            - weather: %s
            - location: %s
            - userTags: %s
            - inferredStyleMode: %s
            - inferredSeason: %s
            - currentTemp: %.1f
            """.formatted(
            request.inputText(),
            request.imageUrl(),
            request.weather(),
            request.location(),
            tags,
            context.styleMode(),
            context.season(),
            context.currentTemp()
        );
    }

    // 응답 텍스트에서 JSON 오브젝트 부분만 추출한다.
    private String extractJson(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("No JSON object found in Gemini response.");
        }
        return cleaned.substring(start, end + 1);
    }
}
