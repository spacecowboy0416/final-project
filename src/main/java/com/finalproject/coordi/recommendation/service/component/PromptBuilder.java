package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    private static final String KOREAN_INPUT_MARKER = "입력값:";
    private static final String ENGLISH_INPUT_MARKER = "Input:";

    @Value("${recommendation.prompt.template:classpath:prompts/recommendation/airequest-prompt-kr.txt}")
    private Resource promptTemplate;

    /**
     * recommendation 요청과 Redis에서 조회한 날씨 컨텍스트를 합쳐
     * Gemini 캐시 가능한 정적 prefix와 요청별 동적 입력 prompt를 분리한다.
     */
    public PromptPayload build(BlueprintRequestDto request, WeatherFetcher.WeatherContext weather) {
        try {
            String template = loadTemplateText();
            TemplateSections templateSections = splitTemplate(template);
            return new PromptPayload(
                templateSections.cacheablePrompt(),
                String.format(
                    templateSections.dynamicPromptTemplate(),
                    request.naturalText(),
                    weather.scheduleTime(),
                    safeDouble(weather.temperature()),
                    safeDouble(weather.feelsLike()),
                    safeText(weather.weatherStatus()),
                    safeText(weather.rainProbability()),
                    safeText(weather.weatherSource()),
                    safeText(weather.districtName()),
                    safeDouble(weather.latitude()),
                    safeDouble(weather.longitude()),
                    buildLocationText(weather),
                    resolveImageInputStatus(request.imageData())
                )
            );
        } catch (IOException exception) {
            throw new IllegalStateException("AI 프롬프트 템플릿을 읽지 못했습니다.", exception);
        }
    }

    /**
     * Gemini cachedContents 워밍에 사용할 정적 프롬프트 prefix를 반환한다.
     * 이 메서드는 사용자 입력/날씨 같은 동적 데이터를 포함하지 않는 영역만 추출하므로
     * 캐시 키의 안정성을 유지하면서 warm-up 시점과 요청 시점의 계약을 일치시킨다.
     */
    public String loadCacheablePrompt() {
        try {
            return splitTemplate(loadTemplateText()).cacheablePrompt();
        } catch (IOException exception) {
            throw new IllegalStateException("AI 프롬프트 템플릿을 읽지 못했습니다.", exception);
        }
    }

    private String loadTemplateText() throws IOException {
        return new String(promptTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private TemplateSections splitTemplate(String template) {
        int inputMarkerIndex = template.indexOf(KOREAN_INPUT_MARKER);
        String marker = KOREAN_INPUT_MARKER;
        if (inputMarkerIndex < 0) {
            inputMarkerIndex = template.indexOf(ENGLISH_INPUT_MARKER);
            marker = ENGLISH_INPUT_MARKER;
        }

        if (inputMarkerIndex < 0) {
            return new TemplateSections("", template);
        }

        return new TemplateSections(
            template.substring(0, inputMarkerIndex).trim(),
            template.substring(inputMarkerIndex).replaceFirst("^" + java.util.regex.Pattern.quote(marker), marker)
        );
    }

    private String buildLocationText(WeatherFetcher.WeatherContext weather) {
        String districtName = safeText(weather.districtName());
        String placeName = safeText(weather.placeName());
        String addressName = safeText(weather.addressName());
        return String.join(" | ", districtName, placeName, addressName);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String safeDouble(Double value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String resolveImageInputStatus(BlueprintRequestDto.ImageData imageData) {
        if (imageData == null) {
            return "missing";
        }

        byte[] imageBytes = imageData.imageBytes();
        if (imageBytes == null || imageBytes.length == 0) {
            return "missing";
        }

        return "success";
    }

    /**
     * 정적 프롬프트 prefix와 요청별 입력 prompt를 분리해 Gemini cachedContents에 활용하기 위한 모델이다.
     */
    public record PromptPayload(
        String cacheablePrompt,
        String requestPrompt
    ) {
    }

    private record TemplateSections(
        String cacheablePrompt,
        String dynamicPromptTemplate
    ) {
    }
}

