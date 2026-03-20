package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.policy.PayloadPolicy;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.WeatherInput;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto.ImageContext;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto.WeatherContext;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;

import java.util.Base64;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 인바운드 API 요청 DTO를 recommendation 내부 요청 모델로 변환한다.
 */
@Component
public class UserRequestMapper {
    public PayloadDto map(UserRequestDto source) {
        if (source == null) {
            return null;
        }
        return new PayloadDto(
            source.naturalText(),
            toGenderType(source.gender()),
            toWeather(source.weather()),
            toImageData(source.imageBase64(), source.imageMimeType())
        );
    }

    private GenderType toGenderType(GenderType source) {
        return source;
    }

    private WeatherContext toWeather(WeatherInput source) {
        if (source == null) {
            return null;
        }
        return new WeatherContext(
            source.temperature(),
            source.feelsLike(),
            source.status() == null ? null : source.status().name(),
            null,
            PayloadPolicy.USER_PROVIDED_WEATHER_SOURCE
        );
    }

    private ImageContext toImageData(
        String imageBase64,
        String imageMimeType
    ) {
        if (!StringUtils.hasText(imageBase64)) {
            return null;
        }
        try {
            return new ImageContext(
                Base64.getDecoder().decode(imageBase64),
                imageMimeType
            );
        } catch (IllegalArgumentException exception) {
            throw new RecommendationException.ValidationException(
                ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT,
                exception
            );
        }
    }
}

