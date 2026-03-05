package com.finalproject.coordi.recommendation.controller;

import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import com.finalproject.coordi.recommendation.dto.RecommendationResponse;
import com.finalproject.coordi.recommendation.service.OrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class RecommendationController {
    private final OrchestratorService orchestratorService;

    public RecommendationController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    // 기본 홈 페이지 진입점.
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 추천 테스트 페이지 진입점(메인 경로).
    @GetMapping("/recommend")
    public String recommendPage() {
        return "recommend-test";
    }

    // 추천 테스트 페이지 진입점(명시 경로).
    @GetMapping("/recommend/test")
    public String recommendTestPage() {
        return "recommend-test";
    }

    // 추천 API(실서비스 모드): fallback 없이 Gemini 응답을 강제한다.
    @PostMapping("/api/recommendations")
    @ResponseBody
    public ResponseEntity<RecommendationResponse> recommendReal(
        @Valid @RequestBody RecommendationRequest request
    ) {
        return ResponseEntity.ok(orchestratorService.recommendReal(request));
    }

    // 추천 API(테스트 모드): Gemini 실패 시 fallback DRAFT를 허용한다.
    @PostMapping("/api/recommendations/test")
    @ResponseBody
    public ResponseEntity<RecommendationResponse> recommendTest(
        @Valid @RequestBody RecommendationRequest request
    ) {
        return ResponseEntity.ok(orchestratorService.recommendTest(request));
    }
}
