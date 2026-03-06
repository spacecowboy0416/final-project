package com.finalproject.coordi.recommendation.controller;

import com.finalproject.coordi.recommendation.dto.CoordinationRequest;
import com.finalproject.coordi.recommendation.dto.CoordinationResponse;
import com.finalproject.coordi.recommendation.service.CoordinationOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class RecommendationController {
    private final CoordinationOrchestrator orchestratorService;

    // 기본 홈 페이지 진입점.
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 추천 테스트 페이지 진입점(메인 경로).
    @GetMapping("/recommend")
    public String recommendPage() {
        return "index";
    }

    // 추천 API: Gemini 결과를 기반으로 추천 결과를 생성한다.
    @PostMapping("/api/recommendations")
    @ResponseBody
    public ResponseEntity<CoordinationResponse> recommend(
        @Valid @RequestBody CoordinationRequest request
    ) {
        return ResponseEntity.ok(orchestratorService.coordinate(request));
    }
}
