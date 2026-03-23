package com.finalproject.coordi.recommendation.service.payload;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto.ImageContext;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto.WeatherContext;
import org.junit.jupiter.api.Test;

class UserRequestNormalizerTest {

    private final UserRequestNormalizer normalizer = new UserRequestNormalizer();

    @Test
    void 유효한Payload를_정규화한다() {
        byte[] imageBytes = new byte[] {1, 2, 3};
        PayloadDto payload = new PayloadDto(
            "출근룩 추천",
            GenderType.FEMALE,
            new WeatherContext(24.0, 22.5, "CLEAR", "low", "USER_PROVIDED"),
            new ImageContext(imageBytes, "image/png")
        );

        var result = normalizer.normalize(payload);

        assertEquals("출근룩 추천", result.naturalText());
        assertEquals("female", result.gender());
        assertEquals("22.5", result.feelsLike());
        assertEquals("CLEAR", result.weatherStatus());
        assertEquals("low", result.rainProbability());
        assertArrayEquals(imageBytes, result.imageData().imageBytes());
        assertEquals("image/png", result.imageData().mimeType());
    }
}
