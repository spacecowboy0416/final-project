package com.finalproject.coordi.recommendation.dto.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * recommendation 파이프라인이 AI blueprint 생성을 시작할 때 받는 최상위 요청 DTO.
 * 자연어 요청, 추천 기준 시각, Redis 날씨 매핑에 사용할 위치 정보, 업로드 이미지를 함께 전달한다.
 */
public record BlueprintInputDto(
    @NotBlank
    @Size(max = 1000)
    String naturalText,
    @NotNull
    OffsetDateTime scheduleTime,
    @Valid @NotNull
    LocationInfo location,
    @Valid @NotNull
    ImageData imageData
) {
    /**
     * Gemini 입력 스키마 계약에서 사용하는 축약 입력 모델.
     * location 정보는 비즈니스/영속 레이어 전용으로 관리하고 AI 스키마에서는 제외한다.
     */
    public record GeminiInputSchema(
        @NotBlank
        @Size(max = 1000)
        String naturalText,
        @NotNull
        OffsetDateTime scheduleTime,
        @Valid @NotNull
        WeatherInfo weather,
        @Valid @NotNull
        ImageData imageData
    ) {
    }

    public GeminiInputSchema toGeminiInputSchema(WeatherInfo weather) {
        return new GeminiInputSchema(
            naturalText,
            scheduleTime,
            weather,
            imageData
        );
    }

    /**
     * Redis 서버에 캐싱된 날씨 정보를 구/군 기준으로 매핑하기 위해 보내는 위치 요청 모델.
     * districtName은 Kakao SDK가 위도/경도에서 해석한 행정구역명이며, 날씨 조회의 실제 캐시 키로 사용된다.
     */
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
    ) {}

    /**
     * Redis에서 조회한 날씨 요약을 AI 입력 계약에 담기 위한 모델.
     */
    public record WeatherInfo(
        Double temperature,
        Double feelsLike,
        String weatherStatus,
        String rainProbability,
        String weatherSource
    ) {
    }

    /**
     * AI blueprint 생성 전에 업로드 이미지를 전달하기 위한 요청 모델.
     * imageBytes는 원본 파일의 바이너리 본문, mimeType은 파일 형식을 식별하기 위한 값이다.
     */
    public record ImageData(
        @NotNull
        @Size(min = 1, max = 10_485_760)
        byte[] imageBytes,
        @NotBlank String mimeType
    ) {}
}
