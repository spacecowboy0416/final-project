package com.finalproject.coordi.recommendation.infra.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.service.blueprint.AiPort;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

/**
 * Gemini SDK를 호출해 recommendation blueprint JSON을 생성하는 어댑터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAdapter implements AiPort {
    private final ObjectMapper objectMapper;
    private final Client geminiClient;
    private final GeminiProperties geminiProperties;
    private final GeminiSchemaProvider schemaProvider;

    /**
     * 입력 DTO와 출력 스키마를 사용해 Gemini blueprint 출력 DTO를 반환한다.
     */
    @Override
    public RawBlueprintDto generateBlueprint(PayloadDto payload) {
        validatePayload(payload);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        GenerateContentConfig generationConfig = GenerateContentConfig.builder()
            .responseMimeType(schemaProvider.responseMimeType())
            .systemInstruction(Content.fromParts(Part.fromText(payload.systemPrompt())))
            .responseSchema(schemaProvider.getOutputSchema())
            .build();

        Content requestContent = Content.fromParts(
            Part.fromText(payload.userPrompt()),
            toInlineDataPart(payload.imageData().mimeType(), payload.imageData().imageBytes())
        );

        try {
            GenerateContentResponse geminiResponse = geminiClient.models.generateContent(
                geminiProperties.getModel(),
                requestContent,
                generationConfig
            );
            stopWatch.stop();
            log.info(
                "gemini request completed model={} promptChars={} imageBytes={} elapsedMs={}",
                geminiProperties.getModel(),
                payload.userPrompt().length(),
                payload.imageData().imageBytes().length,
                stopWatch.getTotalTimeMillis()
            );
            logUsage(geminiResponse.usageMetadata().orElse(null));
            return parseResponse(geminiResponse);
        } catch (Exception exception) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            throw mapToCustomException(exception);
        }
    }

    private void validatePayload(PayloadDto payload) {
        if (payload == null
            || !StringUtils.hasText(payload.systemPrompt())
            || !StringUtils.hasText(payload.userPrompt())
            || payload.imageData() == null
            || !StringUtils.hasText(payload.imageData().mimeType())
            || payload.imageData().imageBytes() == null
            || payload.imageData().imageBytes().length == 0) {
            throw new RecommendationException.AdapterException(
                "Gemini payload 입력이 유효하지 않습니다. systemPrompt/userPrompt/imageData를 확인하세요.",
                com.finalproject.coordi.exception.ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT
            );
        }
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
     * Gemini 응답 본문을 blueprint DTO로 파싱한다.
     */
    private RawBlueprintDto parseResponse(GenerateContentResponse geminiResponse) {
        String responseJson = geminiResponse == null ? null : geminiResponse.text();
        if (!StringUtils.hasText(responseJson)) {
            throw RecommendationException.llmResponseEmpty();
        }

        try {
            return objectMapper.readValue(responseJson, RawBlueprintDto.class);
        } catch (Exception exception) {
            log.error(RecommendationException.geminiBlueprintParseFailedLogMessage(), responseJson, exception);
            throw RecommendationException.llmBlueprintParseFailed(exception);
        }
    }

    /**
     * Gemini 사용량 메타데이터가 있을 때 토큰 사용량을 로그로 남긴다.
     */
    private void logUsage(GenerateContentResponseUsageMetadata usageMetadata) {
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
     * 예외 타입에 맞춰 커스텀 예외 객체로 변환한다.
     */
    private RecommendationException.AdapterException mapToCustomException(Exception exception) {
        return switch (findMappableException(exception)) {
            case ClientException e -> RecommendationException.geminiClientError(e);
            case ServerException e -> RecommendationException.geminiServerError(e);
            case GenAiIOException e -> RecommendationException.geminiIoError(e);
            case ApiException e -> RecommendationException.geminiApiError(e);
            case RestClientResponseException e -> RecommendationException.geminiApiCallFailed(e);
            default -> RecommendationException.geminiApiCallUnexpected(exception);
        };
    }

    private Exception findMappableException(Exception exception) {
        if (exception instanceof ClientException
            || exception instanceof ServerException
            || exception instanceof GenAiIOException
            || exception instanceof ApiException
            || exception instanceof RestClientResponseException) {
            return exception;
        }
        if (exception.getCause() instanceof Exception cause) {
            return findMappableException(cause);
        }
        return exception;
    }
}
