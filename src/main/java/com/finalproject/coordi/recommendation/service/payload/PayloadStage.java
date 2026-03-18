package com.finalproject.coordi.recommendation.service.payload;

import static com.finalproject.coordi.recommendation.domain.prompt.RecommendationPromptTemplates.SYSTEM_PROMPT_EN;
import static com.finalproject.coordi.recommendation.domain.prompt.RecommendationPromptTemplates.USER_PROMPT_EN;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.UserRequest;
import com.finalproject.coordi.recommendation.dto.internal.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI 입력 payload 완성 단계를 한곳에 묶는다.
 */
@Component
@RequiredArgsConstructor
public class PayloadStage {
    private final PayloadBuilder payloadBuilder;
    private final UserRequestMapper userRequestMapper;

    @LogStage("payload.build")
    public PayloadStageResult build(UserRequestDto UserRequest) {
        UserRequest mappedUserRequest = userRequestMapper.map(UserRequest);
        Weather weather = mappedUserRequest.weather();
        PayloadDto payload = payloadBuilder.build(
            SYSTEM_PROMPT_EN,
            USER_PROMPT_EN,
            mappedUserRequest.naturalText(),
            mappedUserRequest.gender(),
            weather,
            mappedUserRequest.scheduleTime()
        );
        return new PayloadStageResult(payload);
    }

    public record PayloadStageResult(
        PayloadDto payload
    ) {
    }
}
