package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FullPromptBuilder {
    @Value("${recommendation.prompt.system-template:classpath:prompts/recommendation/system-prompt-eng.txt}")
    private Resource systemPromptTemplate;

    @Value("${recommendation.prompt.user-template:classpath:prompts/recommendation/user-prompt-eng.txt}")
    private Resource userPromptTemplate;

    /**
     * recommendation 요청과 Redis에서 조회한 날씨 컨텍스트를 합쳐
     * Gemini 요청용 프롬프트를 만든다.
     */
    public FullPrompt build(BlueprintInputDto request, WeatherFetcher.WeatherContext weather) {
        try {
            String systemPrompt = readTemplate(systemPromptTemplate);
            String userTemplate = readTemplate(userPromptTemplate);
            String userPrompt = String.format(
                userTemplate,
                request.naturalText(),
                safeGender(request.gender()),
                weather.scheduleTime(),
                safeDouble(weather.temperature()),
                safeDouble(weather.feelsLike()),
                safeText(weather.weatherStatus()),
                safeText(weather.rainProbability()),
                safeText(weather.weatherSource())
            );
            return new FullPrompt(
                systemPrompt,
                userPrompt,
                request.imageData()
            );
        } catch (IOException exception) {
            throw RecommendationException.promptTemplateReadFailed(exception);
        }
    }

    /// Resource에서 프롬프트 템플릿을 읽어 문자열로 반환한다.
    private String readTemplate(Resource templateResource) throws IOException {
        return new String(templateResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // null 방어
    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String safeDouble(Double value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safeGender(BlueprintInputDto.GenderType gender) {
        return gender == null ? "" : gender.name().toLowerCase();
    }

    public record FullPrompt(
        String systemPrompt,
        String userPrompt,
        BlueprintInputDto.ImageData imageData
    ) {
    }
}
