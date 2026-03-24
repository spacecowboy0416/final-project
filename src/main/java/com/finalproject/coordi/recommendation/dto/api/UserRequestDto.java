package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.config.annotation.ValidBase64;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * recommendation 인바운드 입력 계약 DTO.
 */
public record UserRequestDto(
    @NotBlank
    @Size(max = 1000)
    String naturalText,
    @NotNull
    GenderType gender,
    @Valid
    @NotNull
    WeatherInput weather,
    @NotBlank
    @ValidBase64(minBytes = 1)
    String imageBase64,
    String imageMimeType,
    Boolean brandEnabled
) {
    public record PayloadDto(
        String systemPrompt,
        String userPrompt,
        PayloadImageData imageData
    ) {
        public record PayloadImageData(
            byte[] imageBytes,
            String mimeType
        ) {
        }
    }

    public record WeatherInput(
        @NotNull WeatherStatusType status,
        @NotNull Double temperature,
        @NotNull Double feelsLike
    ) {
    }
}
