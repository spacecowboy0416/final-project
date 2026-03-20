package com.finalproject.coordi.recommendation.service.payload;

import static com.finalproject.coordi.recommendation.domain.prompt.RecommendationPromptTemplates.SYSTEM_PROMPT_EN;
import static com.finalproject.coordi.recommendation.domain.prompt.RecommendationPromptTemplates.USER_PROMPT_EN;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto;
import com.finalproject.coordi.recommendation.service.payload.UserRequestNormalizer.NormalizedUserRequest;

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
    private final UserRequestNormalizer userRequestNormalizer;

    @LogStage("payload.build")
    public UserRequestDto.PayloadDto build(UserRequestDto userRequest) {
        PayloadDto payloadRequestDto = userRequestMapper.map(userRequest);
        NormalizedUserRequest normalizedBuildInput = userRequestNormalizer.normalize(payloadRequestDto);
        UserRequestDto.PayloadDto payload = payloadBuilder.build(
            SYSTEM_PROMPT_EN,
            USER_PROMPT_EN,
            normalizedBuildInput
        );
        return payload;
    }
}


