package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.domain.policy.PayloadPolicy;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto.ImageContext;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto.WeatherContext;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import java.util.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 인바운드 API 요청 DTO를 recommendation 내부 요청 모델로 변환한다.
 */
@Component
public class UserRequestMapper {
    public PayloadDto map(UserRequestDto source) {
        if (source == null) {
            
            throw new RecommendationException.ValidationException(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT);
        }

        return new PayloadDto(
            source.naturalText(),
            source.gender(),
            toWeather(source.weather()),
            toImageData(source.imageBase64(), source.imageMimeType())
        );
    }

    private WeatherContext toWeather(UserRequestDto.WeatherInput source) {
        return new WeatherContext(
            source.temperature(),
            source.feelsLike(),
            source.status().name(),
            PayloadPolicy.EMPTY_TEXT,
            PayloadPolicy.USER_PROVIDED_WEATHER_SOURCE
        );
    }

    private ImageContext toImageData(
        String imageBase64,
        String imageMimeType
    ) {
        if (!StringUtils.hasText(imageBase64)) {
            
            throw new RecommendationException.ValidationException(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT);
        }
        try {
            String normalizedMimeType = StringUtils.hasText(imageMimeType)
                ? imageMimeType
                : PayloadPolicy.DEFAULT_IMAGE_MIME_TYPE;
            return new ImageContext(
                Base64.getDecoder().decode(imageBase64),
                normalizedMimeType
            );
        } catch (IllegalArgumentException exception) {
            throw new RecommendationException.ValidationException(
                ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT,
                exception
            );
        }
    }
}

