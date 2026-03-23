package com.finalproject.coordi.recommendation.service.payload;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.WeatherInput;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class UserRequestMapperTest {

    private final UserRequestMapper mapper = new UserRequestMapper();

    @Test
    void source가Null이면_검증예외를던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> mapper.map(null)
        );

        assertEquals(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void 정상요청이면_핵심필드를매핑한다() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        UserRequestDto request = new UserRequestDto(
            "요청",
            GenderType.MALE,
            validWeather(),
            Base64.getEncoder().encodeToString(bytes),
            "image/png",
            Boolean.TRUE
        );

        var result = mapper.map(request);

        assertEquals("요청", result.naturalText());
        assertEquals(GenderType.MALE, result.gender());
        assertEquals("CLEAR", result.weather().weatherStatus());
        assertArrayEquals(bytes, result.image().imageBytes());
        assertEquals("image/png", result.image().mimeType());
    }

    @Test
    void 이미지가없으면_검증예외를던진다() {
        UserRequestDto request = new UserRequestDto(
            "요청",
            GenderType.UNISEX,
            validWeather(),
            null,
            null,
            Boolean.TRUE
        );

        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> mapper.map(request)
        );

        assertEquals(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void 이미지MimeType이없으면_기본MimeType을사용한다() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        UserRequestDto request = new UserRequestDto(
            "요청",
            GenderType.UNISEX,
            validWeather(),
            Base64.getEncoder().encodeToString(bytes),
            " ",
            Boolean.TRUE
        );

        var result = mapper.map(request);

        assertArrayEquals(bytes, result.image().imageBytes());
        assertEquals(PayloadPolicy.DEFAULT_IMAGE_MIME_TYPE, result.image().mimeType());
        assertEquals(PayloadPolicy.EMPTY_TEXT, result.weather().rainProbability());
    }

    @Test
    void 잘못된Base64면_검증예외를던진다() {
        UserRequestDto request = new UserRequestDto(
            "요청",
            GenderType.MALE,
            validWeather(),
            "not-base64",
            "image/png",
            Boolean.TRUE
        );

        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> mapper.map(request)
        );

        assertEquals(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT, exception.getErrorCode());
    }

    private WeatherInput validWeather() {
        return new WeatherInput(WeatherStatusType.CLEAR, 22.0, 21.0);
    }
}
