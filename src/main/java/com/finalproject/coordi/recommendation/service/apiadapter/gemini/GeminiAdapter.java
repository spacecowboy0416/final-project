package com.finalproject.coordi.recommendation.service.apiadapter.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.service.apiport.AiPort;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.IntegratedPrompt;
import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * Gemini SDK를 호출해 recommendation blueprint JSON을 생성하는 어댑터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAdapter implements AiPort {
    private final ObjectMapper objectMapper;
    private final GeminiProperties geminiApiProperties;
    private final GeminiSchemaProvider schemaProvider;
    @Value("${GEMINI_RESPONSE_MIME_TYPE:application/json}")
    private String recommendationResponseMimeType;

    /**
     * 입력 DTO와 출력 스키마를 사용해 Gemini blueprint 출력 DTO를 반환한다.
     */
    @Override
    public BlueprintOutputDto generateBlueprint(IntegratedPrompt promptPayload, BlueprintInputDto.GeminiInputSchema inputDto) {
        long requestStartedAt = System.nanoTime();
        GeminiTarget target = resolveGeminiTarget();

        GenerateContentConfig config = GenerateContentConfig.builder()
            .responseMimeType(recommendationResponseMimeType)
            .systemInstruction(Content.fromParts(Part.fromText(promptPayload.systemPrompt())))
            .responseSchema(schemaProvider.getOutputSchema())
            .build();

        Content userContent = Content.fromParts(
            Part.fromText(promptPayload.userPrompt()),
            toInlineDataPart(inputDto.imageData().mimeType(), inputDto.imageData().imageBytes())
        );

        GenerateContentResponse response;
        try (Client client = createClient(target)) {
            response = client.models.generateContent(target.model(), userContent, config);
            log.info(
                "gemini request completed model={} promptChars={} imageBytes={} elapsedMs={}",
                target.model(),
                promptPayload.userPrompt() == null ? 0 : promptPayload.userPrompt().length(),
                inputDto.imageData() == null || inputDto.imageData().imageBytes() == null ? 0 : inputDto.imageData().imageBytes().length,
                (System.nanoTime() - requestStartedAt) / 1_000_000
            );
        } catch (Exception exception) {
            throw handleGeminiException(exception);
        }

        String responseText = response == null ? null : response.text();
        if (responseText == null || responseText.isBlank()) {
            throw RecommendationException.llmResponseEmpty();
        }

        try {
            logUsageMetadata(response.usageMetadata().orElse(null));
            return objectMapper.readValue(responseText, BlueprintOutputDto.class);
        } catch (Exception exception) {
            log.error(RecommendationException.geminiBlueprintParseFailedLogMessage(), responseText, exception);
            throw RecommendationException.llmBlueprintParseFailed(exception);
        }
    }

    /**
     * 선택된 Gemini 대상 설정(apiKey, endpoint)을 사용해 SDK Client를 생성한다.
     */
    private Client createClient(GeminiTarget target) {
        Client.Builder builder = Client.builder().apiKey(target.apiKey());
        if (hasText(target.endpoint())) {
            builder.httpOptions(HttpOptions.builder().baseUrl(target.endpoint()).build());
        }
        return builder.build();
    }

    /**
     * 업로드 이미지를 Gemini inline_data 파트로 변환한다.
     */
    private Part toInlineDataPart(String mimeType, byte[] imageBytes) {
        return Part.builder()
            .inlineData(Blob.builder().mimeType(mimeType).data(imageBytes).build())
            .build();
    }

    /**
     * 발생한 예외의 정확한 타입을 판별해 로그를 남기고 커스텀 예외로 변환한다.
     */
    private RecommendationException.AdapterException handleGeminiException(Exception exception) {
        RecommendationException.AdapterException mappedException = mapToCustomException(exception);
        log.error("Gemini API Error: {}", mappedException.getMessage(), exception);
        return mappedException;
    }

    /**
     * 예외 타입에 맞춰 커스텀 예외 객체로 변환한다.
     */
    private RecommendationException.AdapterException mapToCustomException(Exception exception) {
        Exception mappedTarget = findMappableException(exception);

        if (mappedTarget instanceof ClientException e) {
            return RecommendationException.geminiClientError(e);
        }
        if (mappedTarget instanceof ServerException e) {
            return RecommendationException.geminiServerError(e);
        }
        if (mappedTarget instanceof GenAiIOException e) {
            return RecommendationException.geminiIoError(e);
        }
        if (mappedTarget instanceof ApiException e) {
            return RecommendationException.geminiApiError(e);
        }
        if (mappedTarget instanceof RestClientResponseException e) {
            return RecommendationException.geminiApiCallFailed(e);
        }
        return RecommendationException.geminiApiCallUnexpected(mappedTarget);
    }

    /**
     * 래핑된 예외 체인을 따라 내려가며 Gemini 매핑 가능한 예외를 우선 탐색한다.
     */
    private Exception findMappableException(Exception exception) {
        Throwable current = exception;
        int depth = 0;
        while (current != null && depth < 10) {
            if (current instanceof ClientException
                || current instanceof ServerException
                || current instanceof GenAiIOException
                || current instanceof ApiException
                || current instanceof RestClientResponseException) {
                return (Exception) current;
            }
            current = current.getCause();
            depth++;
        }
        return exception;
    }

    /**
     * Gemini 사용량 메타데이터가 있을 때 토큰 사용량을 로그로 남긴다.
     */
    private void logUsageMetadata(GenerateContentResponseUsageMetadata usageMetadata) {
        if (usageMetadata == null) {
            return;
        }

        log.info(
            "gemini usage promptTokens={} candidatesTokens={} totalTokens={}",
            usageMetadata.promptTokenCount().orElse(0),
            usageMetadata.candidatesTokenCount().orElse(0),
            usageMetadata.totalTokenCount().orElse(0)
        );
    }

    /**
     * application.yml의 단일 Gemini 설정(external.api.gemini)으로 generateContent 대상을 결정한다.
     */
    private GeminiTarget resolveGeminiTarget() {
        return new GeminiTarget(
            geminiApiProperties.getEndpoint(),
            geminiApiProperties.getModel(),
            geminiApiProperties.getKey()
        );
    }

    /**
     * null/blank 문자열 여부를 판별한다.
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record GeminiTarget(
        String endpoint,
        String model,
        String apiKey
    ) {
    }
}
