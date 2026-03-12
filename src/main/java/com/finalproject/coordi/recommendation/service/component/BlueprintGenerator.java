package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.service.apiport.AiPort;
import com.finalproject.coordi.recommendation.service.component.PromptBuilder.IntegratedPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlueprintGenerator {
    private final AiPort aiPort;

    /**
     * PromptBuilder가 만든 프롬프트와 업로드 이미지를 입력 DTO로 묶어 Gemini에 전달한다.
     */
    public BlueprintOutputDto generate(
        BlueprintInputDto request,
        WeatherFetcher.WeatherContext weather,
        IntegratedPrompt integratedPrompt
    ) {
        return aiPort.generateBlueprint(
            integratedPrompt,
            request.toGeminiInputSchema(weather.toWeatherInfo())
        );
    }
}
