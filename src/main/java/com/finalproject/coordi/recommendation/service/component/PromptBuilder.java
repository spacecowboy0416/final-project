package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    public String build(BlueprintRequestDto request, WeatherFetcher.WeatherSnapshot weather) {
        // TODO: 입력 데이터와 날씨 정보를 합쳐 AI 프롬프트를 구성한다.
        return null;
    }
}

