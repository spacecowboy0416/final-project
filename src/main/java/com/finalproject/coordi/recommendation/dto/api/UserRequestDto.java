package com.finalproject.coordi.recommendation.dto.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * recommendation 파이프라인의 사용자 인바운드 요청 DTO.
 */
public record UserRequestDto(
    @NotBlank
    @Size(max = 1000)
    String naturalText,
    GenderType gender,
    @NotNull
    OffsetDateTime scheduleTime,
    @Valid @NotNull
    WeatherInput weather,
    String imageBase64,
    String imageMimeType,
    Boolean brandEnabled
) {
    public enum GenderType {
        MALE,
        FEMALE,
        UNISEX
    }

    public record WeatherInput(
        @NotNull com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType status,
        @NotNull Double temperature,
        @NotNull Double feelsLike
    ) {
    }
}
