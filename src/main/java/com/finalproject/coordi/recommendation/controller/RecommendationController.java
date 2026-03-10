package com.finalproject.coordi.recommendation.controller;

import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.service.Orchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
@RequiredArgsConstructor
/**
 * 추천 페이지와 추천 API 엔드포인트를 제공한다.
 */
public class RecommendationController {
    private final Orchestrator orchestratorService;
    @Value("${external.api.kakao-map.key:}")
    private String kakaoMapApiKey;

    // 추천 테스트 페이지를 반환한다.
    @GetMapping("/recommend")
    public String recommendTestPage(Model model) {
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);
        return "recommend-test";
    }

    // 추천 요청을 받아 오케스트레이터로 최종 coordination 응답을 생성한다.
    @PostMapping("/api/recommendations")
    @ResponseBody
    public ResponseEntity<CoordinationOutputDto> recommend(
        @Valid @RequestBody BlueprintRequestDto request
    ) {
        return ResponseEntity.ok(orchestratorService.coordinate(request));
    }
}
