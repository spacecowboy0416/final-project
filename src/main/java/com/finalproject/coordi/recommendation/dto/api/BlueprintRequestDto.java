package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.PrecipitationType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.RainProbabilityType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;
import com.finalproject.coordi.recommendation.exception.ValidationMessages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * AI blueprint 생성 요청 계약 DTO.
 */
public record BlueprintRequestDto(
    @NotBlank(message = ValidationMessages.NATURAL_TEXT_REQUIRED)
    String naturalText,
    @NotNull
    OffsetDateTime scheduleTime,
    @Valid @NotNull
    WeatherInfo weatherInfo,
    @Valid @NotNull
    LocationInfo location,
    @Valid @NotNull
    ImageData imageData
) {
    // 날씨 컨텍스트를 담는 요청 하위 모델
    public record WeatherInfo(
        @NotNull Double temperature,
        @NotNull Double feelsLike,
        @NotNull WeatherStatusType weatherStatus,
        @NotNull PrecipitationType precipitationType,
        @NotNull RainProbabilityType rainProbability
    ) {}

    // 위치 컨텍스트를 담는 요청 하위 모델
    public record LocationInfo(
        String placeName,
        String addressName,
        @NotNull Double latitude,
        @NotNull Double longitude
    ) {}

    // 업로드 이미지 정보를 담는 요청 하위 모델
    public record ImageData(
        @NotNull @Size(min = 1) byte[] imageBytes,
        @NotBlank String mimeType
    ) {}
}
