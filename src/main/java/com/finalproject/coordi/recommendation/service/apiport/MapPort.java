package com.finalproject.coordi.recommendation.service.apiport;

/**
 * 좌표 기반 위치 메타데이터 조회 포트.
 */
public interface MapPort {
    // 위도/경도를 주소 정보로 해석해 위치 메타데이터를 반환한다.
    LocationMeta resolveLocation(
        double latitude,
        double longitude,
        String fallbackPlaceName,
        String fallbackAddressName
    );

    record LocationMeta(
        String placeName,
        String addressName,
        double latitude,
        double longitude
    ) {
    }
}


