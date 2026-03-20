package com.finalproject.coordi.recommendation.controller;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationSaveRequestDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.config.RecommendationImageProperties;
import com.finalproject.coordi.recommendation.service.Orchestrator;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingSearcher;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.infra.gemini.GeminiProperties;
import com.finalproject.coordi.recommendation.infra.map.KakaoMapProperties;
import com.finalproject.coordi.users.annotation.LoginUser;
import com.finalproject.coordi.users.dto.UsersDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
    private final KakaoMapProperties kakaoMapProperties;
    private final GeminiProperties geminiProperties;
    private final RecommendationImageProperties recommendationImageProperties;

    // 추천 입력/출력 페이지를 반환한다.
    @GetMapping("/recommend")
    public String recommendPage(
        @RequestParam(value = "q", required = false) String naturalText,
        Model model
    ) {
        model.addAttribute("initialNaturalText", naturalText == null ? "" : naturalText);
        model.addAttribute("kakaoMapApiKey", kakaoMapProperties.getJsKey());
        model.addAttribute("recommendationImageMaxBytes", recommendationImageProperties.getMaxSize().toBytes());
        return "recommendation/recommend";
    }

    // 디버그 테스트 페이지 진입점 (/recommend-test, /recommend/test 모두 허용)
    @GetMapping({"/recommend-test", "/recommend/test"})
    public String recommendTestPage(Model model) {
        model.addAttribute("kakaoMapApiKey", kakaoMapProperties.getJsKey());
        model.addAttribute("geminiModel", geminiProperties.getModel());
        model.addAttribute("recommendationImageMaxBytes", recommendationImageProperties.getMaxSize().toBytes());
        return "recommendation/recommend-test";
    }

    // 추천 요청을 받아 오케스트레이터로 최종 coordination 응답을 생성한다.
    @PostMapping("/api/recommendations")
    @ResponseBody
    public ResponseEntity<CoordinationOutputDto> recommend(
        @LoginUser UsersDto loginUser,
        @Valid @RequestBody UserRequestDto request
    ) {
        Long userId = loginUser == null ? null : loginUser.getUserId();
        return ResponseEntity.ok(orchestratorService.coordinate(request, userId));
    }

    @PostMapping("/api/recommendations/debug")
    @ResponseBody
    public ResponseEntity<RecommendationDebugResponseDto> recommendDebug(
        @LoginUser UsersDto loginUser,
        @RequestParam(value = "persist", defaultValue = "false") boolean persist,
        @Valid @RequestBody UserRequestDto request
    ) {
        Long userId = loginUser == null ? null : loginUser.getUserId();
        if (persist && userId == null) {
            // 저장 요청은 로그인 사용자만 허용한다.
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        }
        return ResponseEntity.ok(orchestratorService.coordinateDebug(request, userId, persist));
    }

    @PostMapping("/api/recommendations/debug/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveRecommendationDebugResult(
        @LoginUser UsersDto loginUser,
        @Valid @RequestBody RecommendationSaveRequestDto request
    ) {
        Long userId = loginUser == null ? null : loginUser.getUserId();
        if (userId == null) {
            // 저장 요청은 로그인 사용자만 허용한다.
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        }

        orchestratorService.saveDebugResult(request.request(), request.debugResult(), userId);
        return ResponseEntity.ok(Map.of("saved", true));
    }

    @GetMapping("/api/recommendations/debug/shopping")
    @ResponseBody
    public ResponseEntity<List<SearchedProduct>> searchShopping(
        @RequestParam("query") @NotBlank String query
    ) {
        return ResponseEntity.ok(shoppingSearcher.search(query));
    }
}
