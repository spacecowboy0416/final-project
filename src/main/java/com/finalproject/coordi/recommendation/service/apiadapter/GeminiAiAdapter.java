package com.finalproject.coordi.recommendation.service.apiadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import com.finalproject.coordi.recommendation.service.apiport.AiPort;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.PromptPayload;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Gemini generateContent API를 호출해 recommendation blueprint JSON을 생성하는 어댑터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAiAdapter implements AiPort {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, CachedPromptEntry> cachedPromptEntries = new ConcurrentHashMap<>();

    @Value("${external.api.gemini.endpoint}")
    private String geminiEndpoint;

    @Value("${external.api.gemini.model}")
    private String geminiModel;

    @Value("${external.api.gemini.key}")
    private String geminiApiKey;

    @Value("${external.api.gemini-jin.endpoint:}")
    private String geminiJinEndpoint;

    @Value("${external.api.gemini-jin.model:}")
    private String geminiJinModel;

    @Value("${external.api.gemini-jin.key:}")
    private String geminiJinKey;

    @Value("${external.api.gemini.cache.enabled:true}")
    private boolean geminiPromptCacheEnabled;

    @Value("${external.api.gemini.cache.ttl-seconds:3600}")
    private long geminiPromptCacheTtlSeconds;

    /**
     * 프롬프트 문자열과 이미지 바이너리를 Gemini generateContent 요청으로 변환해
     * ai_blueprint JSON을 반환한다.
     */
    @Override
    public JsonNode generateBlueprint(PromptPayload promptPayload, BlueprintRequestDto.ImageData imageData) {
        long requestStartedAt = System.nanoTime();
        GeminiTarget target = resolveGeminiTarget();
        String cachedPromptName = resolveCachedPromptName(promptPayload.cacheablePrompt(), target);
        String requestUri = UriComponentsBuilder
            .fromHttpUrl(target.endpoint())
            .path("/models/" + target.model() + ":generateContent")
            .queryParam("key", target.apiKey())
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        GeminiGenerateContentRequest requestBody = new GeminiGenerateContentRequest(
            cachedPromptName,
            List.of(new GeminiContent(List.of(
                GeminiPart.text(promptPayload.requestPrompt()),
                GeminiPart.inlineData(imageData.mimeType(), imageData.imageBytes())
            )))
        );

        String responseBody;
        try {
            responseBody = restTemplate.postForObject(
                requestUri,
                new HttpEntity<>(requestBody, headers),
                String.class
            );
            log.info(
                "gemini request completed model={} promptChars={} imageBytes={} elapsedMs={}",
                target.model(),
                promptPayload.requestPrompt() == null ? 0 : promptPayload.requestPrompt().length(),
                imageData == null || imageData.imageBytes() == null ? 0 : imageData.imageBytes().length,
                (System.nanoTime() - requestStartedAt) / 1_000_000
            );
        } catch (RestClientResponseException exception) {
            String errorBody = exception.getResponseBodyAsString();
            log.error("Gemini API 호출 실패: status={}, body={}", exception.getStatusCode(), errorBody, exception);
            throw new IllegalStateException(resolveGeminiErrorMessage(exception, errorBody), exception);
        } catch (Exception exception) {
            log.error("Gemini API 호출 중 예외가 발생했습니다.", exception);
            throw new IllegalStateException("Gemini API 호출 중 예외가 발생했습니다.", exception);
        }

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Gemini 응답 본문이 비어 있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            logUsageMetadata(root.path("usageMetadata"));
            JsonNode partsNode = root.path("candidates").path(0).path("content").path("parts");
            String blueprintText = extractBlueprintText(partsNode);
            if (blueprintText == null || blueprintText.isBlank()) {
                log.error("Gemini 응답 파싱 실패: responseBody={}", responseBody);
                throw new IllegalStateException("Gemini 응답에서 blueprint text를 찾지 못했습니다.");
            }
            return objectMapper.readTree(normalizeBlueprintJsonText(blueprintText));
        } catch (Exception exception) {
            log.error("Gemini blueprint JSON 파싱 실패: responseBody={}", responseBody, exception);
            throw new IllegalStateException("Gemini blueprint JSON 파싱에 실패했습니다.", exception);
        }
    }

    private String extractBlueprintText(JsonNode partsNode) {
        if (!partsNode.isArray()) {
            return null;
        }
        for (JsonNode partNode : partsNode) {
            JsonNode textNode = partNode.path("text");
            if (textNode.isTextual() && !textNode.asText().isBlank()) {
                return textNode.asText();
            }
        }
        return null;
    }

    /**
     * Gemini가 markdown code fence나 설명문을 함께 반환해도
     * JSON 본문만 남겨 내부 파서가 읽을 수 있게 정리한다.
     */
    private String normalizeBlueprintJsonText(String rawText) {
        String normalized = rawText.trim();

        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```json\\s*", "");
            normalized = normalized.replaceFirst("^```\\s*", "");
            normalized = normalized.replaceFirst("\\s*```$", "");
            normalized = normalized.trim();
        }

        int jsonStart = normalized.indexOf('{');
        int jsonEnd = normalized.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd >= jsonStart) {
            return normalized.substring(jsonStart, jsonEnd + 1);
        }

        return normalized;
    }

    /**
     * Gemini error body를 recommendation 호출자가 바로 이해할 수 있는 메시지로 변환한다.
     */
    private String resolveGeminiErrorMessage(RestClientResponseException exception, String errorBody) {
        if (errorBody != null) {
            if (errorBody.contains("Unable to process input image")) {
                return "Gemini가 업로드 이미지를 처리하지 못했습니다. 실제 JPG/PNG 이미지로 다시 시도해주세요.";
            }
            if (errorBody.contains("API_KEY_INVALID") || errorBody.contains("API key not valid")) {
                return "Gemini API 키가 올바르지 않습니다.";
            }
            if (errorBody.contains("no longer available to new users")) {
                return "현재 설정한 Gemini 모델을 사용할 수 없습니다. application.yml의 gemini 모델 값을 최신 모델로 바꿔주세요.";
            }
        }

        return "Gemini API 호출에 실패했습니다. status=%s".formatted(exception.getStatusCode());
    }

    private void logUsageMetadata(JsonNode usageMetadataNode) {
        if (!usageMetadataNode.isObject()) {
            return;
        }

        log.info(
            "gemini usage promptTokens={} cachedPromptTokens={} candidatesTokens={} totalTokens={}",
            usageMetadataNode.path("promptTokenCount").asInt(0),
            usageMetadataNode.path("cachedContentTokenCount").asInt(0),
            usageMetadataNode.path("candidatesTokenCount").asInt(0),
            usageMetadataNode.path("totalTokenCount").asInt(0)
        );
    }

    private String resolveCachedPromptName(String cacheablePrompt, GeminiTarget target) {
        if (!geminiPromptCacheEnabled || !hasText(cacheablePrompt)) {
            return null;
        }

        String cacheKey = target.model() + ":" + sha256(cacheablePrompt);
        CachedPromptEntry cachedEntry = cachedPromptEntries.get(cacheKey);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return cachedEntry.cachedContentName();
        }

        try {
            String cacheUri = UriComponentsBuilder
                .fromHttpUrl(target.endpoint())
                .path("/cachedContents")
                .queryParam("key", target.apiKey())
                .build()
                .toUriString();
            GeminiCachedContentCreateRequest cacheRequest = new GeminiCachedContentCreateRequest(
                "models/" + target.model(),
                "recommendation-prompt-cache",
                geminiPromptCacheTtlSeconds + "s",
                List.of(new GeminiContent(List.of(GeminiPart.text(cacheablePrompt))))
            );
            String cacheResponseBody = restTemplate.postForObject(cacheUri, cacheRequest, String.class);
            if (cacheResponseBody == null || cacheResponseBody.isBlank()) {
                return null;
            }

            JsonNode cacheRoot = objectMapper.readTree(cacheResponseBody);
            String cacheName = cacheRoot.path("name").asText(null);
            if (!hasText(cacheName)) {
                return null;
            }

            cachedPromptEntries.put(
                cacheKey,
                new CachedPromptEntry(cacheName, System.currentTimeMillis() + (geminiPromptCacheTtlSeconds * 1000))
            );
            log.info("gemini cached prompt created model={} cacheName={}", target.model(), cacheName);
            return cacheName;
        } catch (Exception exception) {
            log.warn("Gemini prompt cache creation failed. Fallback to uncached request.", exception);
            return null;
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Prompt cache key 해시 생성에 실패했습니다.", exception);
        }
    }

    /**
     * recommendation의 jin 전용 Gemini 설정이 있으면 그 값을 우선 사용하고,
     * 없으면 기본 Gemini 설정으로 generateContent 대상을 결정한다.
     */
    private GeminiTarget resolveGeminiTarget() {
        if (hasText(geminiJinEndpoint) || hasText(geminiJinModel)) {
            return new GeminiTarget(
                hasText(geminiJinEndpoint) ? geminiJinEndpoint : geminiEndpoint,
                hasText(geminiJinModel) ? geminiJinModel : geminiModel,
                hasText(geminiJinKey) ? geminiJinKey : geminiApiKey
            );
        }

        return new GeminiTarget(geminiEndpoint, geminiModel, geminiApiKey);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record GeminiGenerateContentRequest(
        String cachedContent,
        List<GeminiContent> contents
    ) {
    }

    private record GeminiCachedContentCreateRequest(
        String model,
        String displayName,
        String ttl,
        List<GeminiContent> contents
    ) {
    }

    private record GeminiContent(
        List<GeminiPart> parts
    ) {
    }

    private record GeminiPart(
        String text,
        InlineData inlineData
    ) {
        private static GeminiPart text(String value) {
            return new GeminiPart(value, null);
        }

        /**
         * recommendation 요청에서 받은 업로드 원본 이미지를 그대로 base64 인코딩해
         * Gemini inlineData 본문에 담는다.
         */
        private static GeminiPart inlineData(String mimeType, byte[] imageBytes) {
            return new GeminiPart(null, new InlineData(mimeType, java.util.Base64.getEncoder().encodeToString(imageBytes)));
        }
    }

    private record InlineData(
        String mimeType,
        String data
    ) {
    }

    private record GeminiTarget(
        String endpoint,
        String model,
        String apiKey
    ) {
    }

    private record CachedPromptEntry(
        String cachedContentName,
        long expiresAtMillis
    ) {
        private boolean isExpired() {
            return System.currentTimeMillis() >= expiresAtMillis;
        }
    }
}
