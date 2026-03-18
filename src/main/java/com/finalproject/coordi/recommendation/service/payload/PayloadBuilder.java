package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
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
        OffsetDateTime scheduleTime
    ) {
        Weather safeWeather = safeWeather(weather);

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
            new PayloadDto.PayloadImageData(new byte[0], "")
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
}
