package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.internal.ImageData;
import com.finalproject.coordi.recommendation.dto.internal.Weather;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PayloadBuilder {
    public static final String DEFAULT_IMAGE_MIME_TYPE = "image/jpeg";

    public PayloadDto build(
        String systemPrompt,
        String userPromptTemplate,
        String naturalText,
        GenderType gender,
        Weather weather,
        OffsetDateTime scheduleTime,
        ImageData imageData
    ) {
        Weather safeWeather = safeWeather(weather);
        ImageData safeImageData = requireImageData(imageData);

        String userPrompt = String.format(
            userPromptTemplate,
            naturalText,
            safeGender(gender),
            scheduleTime,
            safeDouble(safeWeather.feelsLike()),
            safeText(safeWeather.weatherStatus()),
            safeText(safeWeather.rainProbability())
        );
        return new PayloadDto(
            systemPrompt,
            userPrompt,
            new PayloadDto.PayloadImageData(
                safeImageData.imageBytes(),
                safeImageData.mimeType()
            )
        );
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String safeDouble(Double value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safeGender(GenderType gender) {
        return gender == null ? "" : gender.name().toLowerCase(Locale.ROOT);
    }

    private Weather safeWeather(Weather weather) {
        if (weather == null) {
            return new Weather(null, null, null, null, null);
        }
        return weather;
    }

    private ImageData requireImageData(ImageData imageData) {
        if (imageData == null
            || imageData.imageBytes() == null
            || imageData.imageBytes().length == 0) {
            throw new RecommendationException.ValidationException(ErrorCode.RECOMMENDATION_GEMINI_INVALID_ARGUMENT);
        }

        String mimeType = StringUtils.hasText(imageData.mimeType())
            ? imageData.mimeType()
            : DEFAULT_IMAGE_MIME_TYPE;

        return new ImageData(imageData.imageBytes(), mimeType);
    }
}
