package com.finalproject.coordi.recommendation.dto;

/**
 * 팀 공통 위치 입력 계약 DTO.
 */
public record LocationInput(
    String city,
    String country,
    Double latitude,
    Double longitude
) {
}
