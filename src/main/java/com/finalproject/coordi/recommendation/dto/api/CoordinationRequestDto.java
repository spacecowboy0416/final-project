package com.finalproject.coordi.recommendation.dto.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * AI 코디 추천을 위한 통합 요청 DTO
 */
public record CoordinationRequestDto(
    // 1. 사용자 입력 정보
    @NotBlank(message = "요청 사항을 입력해주세요.") 
    String naturalText,      // "오늘 데이트 가는데 잘 보이고 싶어"
    
    @NotNull 
    OffsetDateTime scheduleTime,

    // 2. 스타일 컨텍스트 (TPO, Mood, Preference)
    @Valid @NotNull 
    StyleContext styleContext,

    // 3. 미리 조회된 날씨 정보 (Phase 1 결과물)
    @Valid @NotNull 
    WeatherInfo weatherInfo,

    // 4. 위치 정보 
    @Valid @NotNull 
    LocationInfo location,

    // 5. 사진 데이터 (바이트 방식)
    @Valid @NotNull 
    PhotoData photo
) {
    /** 스타일 상세 정보를 담는 레코드 */
    public record StyleContext(
        @NotBlank String tpo,              // 예: 데이트, 결혼식, 출근
        @NotBlank String mood,             // 예: 미니멀, 스트릿, 아메카지
        @NotEmpty List<@NotBlank String> preferences // 예: ["무채색 선호", "오버핏 제외"]
    ) {}

    /** 미리 조회되어 화면에 보여졌던 날씨 정보 */
    public record WeatherInfo(
        @NotNull Double temperature,
        @NotNull Double feelsLike, // 체감 온도
        @NotBlank String weatherStatus, // 기상 상태 (맑음, 흐림)
        @NotNull boolean isRaining, // 강우 여부
        @NotNull @Min(0) @Max(100) Integer rainProbability // 강우 확률
    ) {}

    /** 위치 정보 */
    public record LocationInfo(
        String placeName,
        String addressName,
        @NotNull Double latitude,
        @NotNull Double longitude
    ) {}

    /** AI 전송을 위한 사진 바이트 데이터 */
    public record PhotoData(
        @NotNull @Size(min = 1) byte[] imageBytes, // 이미지 바이너리
        @NotBlank String mimeType    // image/jpeg, image/png 등
    ) {}
}
