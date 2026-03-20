package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.domain.policy.PayloadPolicy;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * payload 생성 전에 입력을 정규화한다.
 */
@Component
public class UserRequestNormalizer {
    public NormalizedUserRequest normalize(PayloadDto payloadRequestDto) {
        PayloadDto source = payloadRequestDto == null
            ? new PayloadDto(null, null, null, null)
            : payloadRequestDto;

        PayloadDto.ImageContext imageContext = normalizeImage(source.image());
        PayloadDto.WeatherContext weatherContext = normalizeWeather(source.weather());
        String naturalText = normalizeText(source.naturalText());
        String gender = normalizeGender(source.gender());

        return new NormalizedUserRequest(
            naturalText,
            gender,
            normalizeDouble(weatherContext.feelsLike()),
            normalizeText(weatherContext.weatherStatus()),
            normalizeText(weatherContext.rainProbability()),
            new UserRequestDto.PayloadDto.PayloadImageData(imageContext.imageBytes(), imageContext.mimeType())
        );
    }

    public record NormalizedUserRequest(
        String naturalText,
        String gender,
        String feelsLike,
        String weatherStatus,
        String rainProbability,
        UserRequestDto.PayloadDto.PayloadImageData imageData
    ) {
    }

    private String normalizeText(String value) {
        return value == null ? PayloadPolicy.EMPTY_TEXT : value;
    }

    private String normalizeDouble(Double value) {
        return value == null ? PayloadPolicy.EMPTY_TEXT : String.valueOf(value);
    }

    private String normalizeGender(Enum<?> gender) {
        return gender == null
            ? PayloadPolicy.EMPTY_TEXT
            : gender.name().toLowerCase(Locale.ROOT);
    }

    private PayloadDto.WeatherContext normalizeWeather(PayloadDto.WeatherContext weatherContext) {
        if (weatherContext == null) {
            return new PayloadDto.WeatherContext(null, null, null, null, null);
        }
        return weatherContext;
    }

    private PayloadDto.ImageContext normalizeImage(PayloadDto.ImageContext imageContext) {
        if (imageContext == null) {
            // Stage에서 validator를 먼저 호출하는 것을 전제로 한다.
            return new PayloadDto.ImageContext(new byte[0], PayloadPolicy.DEFAULT_IMAGE_MIME_TYPE);
        }
        if (!StringUtils.hasText(imageContext.mimeType())) {
            return new PayloadDto.ImageContext(
                imageContext.imageBytes(),
                PayloadPolicy.DEFAULT_IMAGE_MIME_TYPE
            );
        }
        return imageContext;
    }
}
