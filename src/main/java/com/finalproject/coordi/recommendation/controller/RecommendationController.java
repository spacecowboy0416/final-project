package com.finalproject.coordi.recommendation.controller;

import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import com.finalproject.coordi.recommendation.service.Orchestrator;
import com.finalproject.coordi.recommendation.service.component.ShoppingSearcher;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ShoppingSearcher shoppingSearcher;
    @Value("${external.api.kakao-map.js-key:}")
    private String kakaoMapJsKey;
    @Value("${external.api.gemini.model:}")
    private String geminiModel;

    // 추천 테스트 페이지를 반환한다.
    @GetMapping("/recommend")
    public String recommendTestPage(Model model) {
        model.addAttribute("kakaoMapApiKey", kakaoMapJsKey);
        model.addAttribute("geminiModel", geminiModel);
        return "recommendation/recommend-test";
    }

    // 추천 요청을 받아 오케스트레이터로 최종 coordination 응답을 생성한다.
    @PostMapping("/api/recommendations")
    @ResponseBody
    public ResponseEntity<CoordinationOutputDto> recommend(
        @Valid @RequestBody BlueprintInputDto request
    ) {
        return ResponseEntity.ok(orchestratorService.coordinate(request));
    }

    @PostMapping("/api/recommendations/debug")
    @ResponseBody
    public ResponseEntity<RecommendationDebugResponseDto> recommendDebug(
        @Valid @RequestBody BlueprintInputDto request
    ) {
        return ResponseEntity.ok(orchestratorService.coordinateDebug(request));
    }

    @GetMapping("/api/recommendations/debug/shopping")
    @ResponseBody
    public ResponseEntity<List<ShoppingProductCandidate>> searchShopping(
        @RequestParam("query") @NotBlank String query,
        @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return ResponseEntity.ok(shoppingSearcher.search(query, limit));
    }
}
