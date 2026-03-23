package com.finalproject.coordi.recommendation.service.payload;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;
import com.finalproject.coordi.recommendation.domain.policy.PayloadPolicy;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.WeatherInput;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class PayloadStageTest {

    private final PayloadStage payloadStage = new PayloadStage(
        new PayloadBuilder(),
        new UserRequestMapper(),
        new UserRequestNormalizer()
    );

    @Test
    void 정상입력이면_payload를완성한다() {
        byte[] imageBytes = "stage-image".getBytes(StandardCharsets.UTF_8);
        UserRequestDto request = new UserRequestDto(
            "봄 출근룩 추천",
            GenderType.MALE,
            new WeatherInput(WeatherStatusType.CLEAR, 18.0, 17.0),
            Base64.getEncoder().encodeToString(imageBytes),
            null,
            Boolean.TRUE
        );

        UserRequestDto.PayloadDto payload = payloadStage.build(request);

        assertNotNull(payload);
        assertNotNull(payload.imageData());
        assertArrayEquals(imageBytes, payload.imageData().imageBytes());
        assertEquals(PayloadPolicy.DEFAULT_IMAGE_MIME_TYPE, payload.imageData().mimeType());
        assertTrue(payload.userPrompt().contains("naturalText: 봄 출근룩 추천"));
        assertTrue(payload.userPrompt().contains("targetGender: male"));
        assertTrue(payload.userPrompt().contains("weather.feelsLike: 17.0"));
        assertTrue(payload.userPrompt().contains("weather.status: CLEAR"));
        assertTrue(payload.userPrompt().contains("weather.rainProbability: " + PayloadPolicy.EMPTY_TEXT));
    }

    @Test
    void mapper검증실패는_stage에서즉시전파된다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> payloadStage.build(null)
        );

        assertEquals(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT, exception.getErrorCode());
    }
}
