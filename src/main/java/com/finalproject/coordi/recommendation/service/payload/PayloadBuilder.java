package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.PayloadDto;
import com.finalproject.coordi.recommendation.service.payload.UserRequestNormalizer.NormalizedUserRequest;
import org.springframework.stereotype.Component;

@Component
public class PayloadBuilder {

    public PayloadDto build(String systemPrompt, String userPromptTemplate, NormalizedUserRequest req) {
        // template.formatted(...)를 사용하여 String.format 정적 메서드 호출을 제거
        String userPrompt = userPromptTemplate.formatted(
            req.naturalText(),
            req.gender(),
            req.feelsLike(),
            req.weatherStatus(),
            req.rainProbability()
        );

        return new PayloadDto(systemPrompt, userPrompt, req.imageData());
    }
}
