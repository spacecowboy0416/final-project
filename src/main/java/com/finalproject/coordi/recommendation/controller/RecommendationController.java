package com.finalproject.coordi.recommendation.controller;

import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationSaveRequestDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.config.RecommendationImageProperties;
import com.finalproject.coordi.exception.auth.AuthFailedException;
import com.finalproject.coordi.recommendation.service.Orchestrator;
import com.finalproject.coordi.recommendation.service.persistent.RecommendationSavePersistence;
import com.finalproject.coordi.recommendation.infra.map.KakaoMapProperties;
import com.finalproject.coordi.users.annotation.LoginUser;
import com.finalproject.coordi.users.dto.UsersDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final RecommendationSavePersistence recommendationSavePersistence;
    private final KakaoMapProperties kakaoMapProperties;
    private final RecommendationImageProperties recommendationImageProperties;

    // 추천 입력/출력 페이지를 반환한다.
    @GetMapping("/recommend")
    public String recommendPage(
        @RequestParam(value = "q", required = false) String naturalText,
        Authentication authentication,
        Model model
    ) {
        model.addAttribute("initialNaturalText", naturalText == null ? "" : naturalText);
        model.addAttribute("kakaoMapApiKey", kakaoMapProperties.getJsKey());
        model.addAttribute("recommendationImageMaxBytes", recommendationImageProperties.getMaxSize().toBytes());
        model.addAttribute("recommendationSaveEnabled", isAuthenticated(authentication));
        return "recommendation/recommend";
    }

    // 추천 요청을 받아 오케스트레이터로 최종 coordination 응답을 생성한다.
    @PostMapping("/api/recommendations")
    @ResponseBody
    public ResponseEntity<CoordinationOutputDto> recommend(
        @Valid @RequestBody UserRequestDto request
    ) {
        return ResponseEntity.ok(orchestratorService.coordinate(request));
    }

    @PostMapping("/api/recommendations/save")
    @ResponseBody
    public ResponseEntity<PersistResponse> saveRecommendation(
        @LoginUser UsersDto loginUser,
        @Valid @RequestBody RecommendationSaveRequestDto request
    ) {
        if (loginUser == null) {
            throw new AuthFailedException();
        }
        Long recId = recommendationSavePersistence.save(loginUser.getUserId(), request);
        return ResponseEntity.ok(new PersistResponse(recId));
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal());
    }

    // 얇은 저장 응답 DTO는 컨트롤러 내부 record로 유지한다.
    public static record PersistResponse(Long recId) {
    }
}
