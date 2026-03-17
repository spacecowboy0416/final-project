package com.finalproject.coordi.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finalproject.coordi.main.dto.MainResponse;
import com.finalproject.coordi.main.dto.WeatherResponse;
import com.finalproject.coordi.main.service.MainService;

import lombok.RequiredArgsConstructor;

/**
 * [메인페이지 기능 흐름]
 * 1. 클라이언트에서 위치(lat, lon) 전달
 * 2. WeatherService를 통해 날씨 정보 조회 (캐시 → 없으면 외부 API)
 * 3. 날씨 기반 카테고리 추천 생성
 * 4. MainResponse로 가공하여 반환 (UI 표시용 데이터)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainController {

    private final MainService mainService;
    
    /**
     * 메인페이지 초기 진입 시 사용하는 요약 API
     * 위치 기준 날씨 + 카테고리 추천 + 화면 표시용 정보를 함께 반환한다.
     */
    @GetMapping("/summary")
    public MainResponse getSummary(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(name = "isDefault", defaultValue = "false") boolean isDefault) {
        return mainService.getSummary(lat, lon, isDefault);
    }
    
    /**
     * 개발용 날씨 미리보기 API
     * 프론트에서 만든 테스트용 날씨 데이터를 받아 메인 응답 형태로 변환한다.
     */
    @PostMapping("/preview")
    public MainResponse preview(@RequestBody WeatherResponse weather) {
        return mainService.preview(weather);
    }
}