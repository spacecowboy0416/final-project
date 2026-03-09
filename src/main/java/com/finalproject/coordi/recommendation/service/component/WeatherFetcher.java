package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import org.springframework.stereotype.Component;

@Component
public class WeatherFetcher {
    public WeatherSnapshot fetch(BlueprintRequestDto request) {
        // TODO: 요청의 위치/시간 기준으로 날씨 정보를 조회한다.
        return null;
    }

    public record WeatherSnapshot(
        Double temperature,
        Double feelsLike,
        String weatherStatus
    ) {
    }
}

