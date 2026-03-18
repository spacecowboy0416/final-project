package com.finalproject.coordi.recommendation.dto.internal;

public record Location(
    String districtName,
    String placeName,
    String addressName,
    Double latitude,
    Double longitude
) {
}
