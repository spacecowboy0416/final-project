package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.internal.ImageData;
import com.finalproject.coordi.recommendation.dto.internal.Weather;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class PayloadBuilder {
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
        ImageData safeImageData = safeImageData(imageData);

        String userPrompt = String.format(
            userPromptTemplate,
            naturalText,
            safeGender(gender),
            scheduleTime,
            safeDouble(safeWeather.temperature()),
            safeDouble(safeWeather.feelsLike()),
            safeText(safeWeather.weatherStatus()),
            safeText(safeWeather.rainProbability()),
            safeText(safeWeather.weatherSource())
        );
        return new PayloadDto(
            systemPrompt,
            userPrompt,
            new PayloadDto.PayloadImageData(safeImageBytes(safeImageData.imageBytes()), safeText(safeImageData.mimeType()))
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

    private ImageData safeImageData(ImageData imageData) {
        if (imageData == null) {
            return new ImageData(new byte[0], "");
        }
        return imageData;
    }

    private byte[] safeImageBytes(byte[] imageBytes) {
        if (imageBytes == null) {
            return new byte[0];
        }
        return imageBytes;
    }
}
