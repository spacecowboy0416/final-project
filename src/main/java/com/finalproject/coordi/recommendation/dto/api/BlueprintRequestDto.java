package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.exception.ValidationMessages;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * recommendation 파이프라인이 AI blueprint 생성을 시작할 때 받는 최상위 요청 DTO.
 * 자연어 요청, 추천 기준 시각, Redis 날씨 매핑에 사용할 위치 정보, 업로드 이미지를 함께 전달한다.
 */
public record BlueprintRequestDto(
    @NotBlank(message = ValidationMessages.NATURAL_TEXT_REQUIRED)
    String naturalText,
    @NotNull
    OffsetDateTime scheduleTime,
    @Valid @NotNull
    LocationInfo location,
    @Valid @NotNull
    ImageData imageData
) {
    /**
     * Redis 서버에 캐싱된 날씨 정보를 구/군 기준으로 매핑하기 위해 보내는 위치 요청 모델.
     * districtName은 Kakao SDK가 위도/경도에서 해석한 행정구역명이며, 날씨 조회의 실제 캐시 키로 사용된다.
     */
    public record LocationInfo(
        @NotBlank String districtName,
        String placeName,
        String addressName,
        @NotNull Double latitude,
        @NotNull Double longitude
    ) {}

    /**
     * AI blueprint 생성 전에 업로드 이미지를 전달하기 위한 요청 모델.
     * imageBytes는 원본 파일의 바이너리 본문, mimeType은 파일 형식을 식별하기 위한 값이다.
     */
    public record ImageData(
        @NotNull @Size(min = 1) byte[] imageBytes,
        @NotBlank String mimeType
    ) {}
}
