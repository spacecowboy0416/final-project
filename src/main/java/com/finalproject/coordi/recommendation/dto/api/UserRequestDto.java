package com.finalproject.coordi.recommendation.dto.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    LocationInfo location,
    @Valid @NotNull
    ImageData imageData
) {
    public enum GenderType {
        MALE,
        FEMALE,
        UNISEX
    }

    public record LocationInfo(
        @NotBlank String districtName,
        String placeName,
        String addressName,
        @NotNull
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        Double latitude,
        @NotNull
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        Double longitude
    ) {
    }

    public record ImageData(
        @NotNull
        @Size(min = 1)
        byte[] imageBytes,
        @NotBlank String mimeType
    ) {
    }
}
