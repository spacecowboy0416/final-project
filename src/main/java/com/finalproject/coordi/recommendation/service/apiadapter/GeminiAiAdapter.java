package com.finalproject.coordi.recommendation.service.apiadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import com.finalproject.coordi.recommendation.service.apiport.AiPort;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.PromptPayload;
import com.finalproject.coordi.recommendation.service.component.ImageProcessor;
import com.finalproject.coordi.recommendation.service.component.ImageProcessor.ProcessedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Gemini generateContent API를 호출해 recommendation blueprint JSON을 생성하는 어댑터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAiAdapter implements AiPort {
    private static final String GEMINI_CONTENT_ROLE_USER = "user";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ImageProcessor imageProcessor;
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
        CachedPromptResolution cachedPromptResolution = resolveCachedPromptName(promptPayload.cacheablePrompt(), target);
        String requestUri = UriComponentsBuilder
            .fromHttpUrl(target.endpoint())
            .path("/models/" + target.model() + ":generateContent")
            .queryParam("key", target.apiKey())
            .build()
            .toUriString();
        ProcessedImage processedImage = imageProcessor.process(imageData);
        if (processedImage.imageBytes() == null || processedImage.imageBytes().length == 0) {
            throw new IllegalStateException("Gemini 전송용 이미지가 비어 있습니다.");
        }

        GeminiGenerateContentRequest requestBody = new GeminiGenerateContentRequest(
            cachedPromptResolution.cachedPromptName(),
            List.of(new GeminiContent(
                GEMINI_CONTENT_ROLE_USER,
                List.of(
                    GeminiPart.text(promptPayload.requestPrompt()),
                    GeminiPart.inlineData(processedImage.mimeType(), processedImage.imageBytes())
                )
            ))
        );

        String responseBody;
        try {
            responseBody = restClient.post()
                .uri(requestUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);
            log.info(
                "gemini request completed model={} promptChars={} cacheStatus={} imageBytesOriginal={} imageBytesProcessed={} imageFallback={} imageReason={} elapsedMs={}",
                target.model(),
                promptPayload.requestPrompt() == null ? 0 : promptPayload.requestPrompt().length(),
                cachedPromptResolution.status(),
                imageData == null || imageData.imageBytes() == null ? 0 : imageData.imageBytes().length,
                processedImage.imageBytes() == null ? 0 : processedImage.imageBytes().length,
                processedImage.fallback(),
                processedImage.reason(),
                (System.nanoTime() - requestStartedAt) / 1_000_000
            );
        } catch (RestClientResponseException exception) {
            String errorBody = exception.getResponseBodyAsString();
            if (shouldRetryWithoutCache(cachedPromptResolution.cachedPromptName(), errorBody)) {
                evictCachedPromptEntryByName(cachedPromptResolution.cachedPromptName());
                GeminiGenerateContentRequest uncachedRequestBody = new GeminiGenerateContentRequest(
                    null,
                    requestBody.contents()
                );
                try {
                    responseBody = restClient.post()
                        .uri(requestUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(uncachedRequestBody)
                        .retrieve()
                        .body(String.class);
                    log.warn(
                        "Gemini cached content rejected due to minimum token constraint. Retried uncached successfully. model={} cacheName={}",
                        target.model(),
                        cachedPromptResolution.cachedPromptName()
                    );
                } catch (RestClientResponseException retryException) {
                    String retryErrorBody = retryException.getResponseBodyAsString();
                    log.error("Gemini API 호출 실패(uncached retry 포함): status={}, body={}", retryException.getStatusCode(), retryErrorBody, retryException);
                    throw new IllegalStateException(resolveGeminiErrorMessage(retryException, retryErrorBody), retryException);
                }
                // uncached 재시도 성공 시 이후 파싱 단계로 진행
            } else {
                log.error("Gemini API 호출 실패: status={}, body={}", exception.getStatusCode(), errorBody, exception);
                throw new IllegalStateException(resolveGeminiErrorMessage(exception, errorBody), exception);
            }
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
            if (errorBody.contains("Cached content is too small")) {
                return "Gemini 캐시 프롬프트 토큰이 최소 기준보다 작습니다. uncached 경로 또는 더 긴 cacheable prompt를 사용하세요.";
            }
            if (errorBody.contains("content.role set")) {
                return "Gemini cached content 포맷이 올바르지 않습니다. role=user 포함 여부를 확인하세요.";
            }
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

    private boolean shouldRetryWithoutCache(String cachedPromptName, String errorBody) {
        return hasText(cachedPromptName) && errorBody != null && errorBody.contains("Cached content is too small");
    }

    private void evictCachedPromptEntryByName(String cachedPromptName) {
        if (!hasText(cachedPromptName)) {
            return;
        }
        cachedPromptEntries.entrySet().removeIf(entry -> cachedPromptName.equals(entry.getValue().cachedContentName()));
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

    /**
     * recommendation 프롬프트 prefix를 Gemini cachedContents에 미리 등록해
     * 첫 사용자 요청이 캐시 생성 RTT를 부담하지 않게 준비한다.
     * 호출자는 warm-up 실패를 비치명으로 취급하며, 실제 요청에서는 uncached로 자동 폴백된다.
     */
    public void warmUpPromptCache(String cacheablePrompt) {
        GeminiTarget target = resolveGeminiTarget();
        CachedPromptResolution resolution = resolveCachedPromptName(cacheablePrompt, target);
        log.info("gemini prompt cache warm-up finished model={} status={}", target.model(), resolution.status());
    }

    private CachedPromptResolution resolveCachedPromptName(String cacheablePrompt, GeminiTarget target) {
        if (!geminiPromptCacheEnabled || !hasText(cacheablePrompt)) {
            return new CachedPromptResolution(null, "disabled-or-empty");
        }

        String cacheKey = target.model() + ":" + sha256(cacheablePrompt);
        CachedPromptEntry cachedEntry = cachedPromptEntries.get(cacheKey);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return new CachedPromptResolution(cachedEntry.cachedContentName(), "hit");
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
                List.of(new GeminiContent(
                    GEMINI_CONTENT_ROLE_USER,
                    List.of(GeminiPart.text(cacheablePrompt))
                ))
            );
            String cacheResponseBody = restClient.post()
                .uri(cacheUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cacheRequest)
                .retrieve()
                .body(String.class);
            if (cacheResponseBody == null || cacheResponseBody.isBlank()) {
                return new CachedPromptResolution(null, "miss-create-empty");
            }

            JsonNode cacheRoot = objectMapper.readTree(cacheResponseBody);
            String cacheName = cacheRoot.path("name").asText(null);
            if (!hasText(cacheName)) {
                return new CachedPromptResolution(null, "miss-create-no-name");
            }

            cachedPromptEntries.put(
                cacheKey,
                new CachedPromptEntry(cacheName, System.currentTimeMillis() + (geminiPromptCacheTtlSeconds * 1000))
            );
            log.info("gemini cached prompt created model={} cacheName={}", target.model(), cacheName);
            return new CachedPromptResolution(cacheName, "miss-created");
        } catch (RestClientResponseException exception) {
            String errorBody = exception.getResponseBodyAsString();
            log.warn(
                "Gemini prompt cache creation failed status={} body={}. Fallback to uncached request.",
                exception.getStatusCode(),
                errorBody,
                exception
            );
            return new CachedPromptResolution(null, "miss-create-failed");
        } catch (Exception exception) {
            log.warn("Gemini prompt cache creation failed. Fallback to uncached request.", exception);
            return new CachedPromptResolution(null, "miss-create-failed");
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
        String role,
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
         * recommendation 이미지 처리 정책(ImageProcessor)을 거친 전송용 이미지를
         * Gemini inlineData 본문에 base64 인코딩해 담는다.
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

    private record CachedPromptResolution(
        String cachedPromptName,
        String status
    ) {
    }
}
