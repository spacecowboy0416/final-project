package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.domain.RecommendationContext;
import com.finalproject.coordi.recommendation.domain.type.SeasonType;
import com.finalproject.coordi.recommendation.domain.type.StyleMode;
import com.finalproject.coordi.recommendation.domain.type.TpoType;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ContextResolver {
    // 입력 요청에서 추천 컨텍스트를 추출한다.
    public RecommendationContext resolve(RecommendationRequest request) {
        double currentTemp = extractTemperature(request);
        TpoType tpoType = inferTpoType(request.inputText());
        StyleMode styleMode = inferStyleMode(request.inputText(), request.userTags());
        SeasonType season = inferSeason(currentTemp);
        return new RecommendationContext(currentTemp, tpoType, styleMode, season);
    }

    // WeatherInput 계약에서 온도 값을 읽는다.
    private double extractTemperature(RecommendationRequest request) {
        if (request.weather() == null || request.weather().temp() == null) {
            return 20.0;
        }
        return request.weather().temp();
    }

    // 프롬프트 텍스트에서 TPO를 판별한다.
    private TpoType inferTpoType(String inputText) {
        String text = inputText.toLowerCase(Locale.ROOT);
        if (text.contains("출근") || text.contains("업무") || text.contains("work")) {
            return TpoType.WORK;
        }
        if (text.contains("운동") || text.contains("헬스") || text.contains("exercise")) {
            return TpoType.EXERCISE;
        }
        if (text.contains("여행") || text.contains("travel")) {
            return TpoType.TRAVEL;
        }
        if (text.contains("데이트") || text.contains("date")) {
            return TpoType.DATE;
        }
        if (text.contains("격식") || text.contains("formal")) {
            return TpoType.FORMAL;
        }
        return TpoType.CASUAL;
    }

    // 프롬프트/태그에서 스타일 모드를 판별한다.
    private StyleMode inferStyleMode(String inputText, List<String> userTags) {
        String joinedTags = userTags == null ? "" : String.join(" ", userTags);
        String text = (inputText + " " + joinedTags).toLowerCase(Locale.ROOT);
        if (text.contains("미니멀") || text.contains("minimal")) {
            return StyleMode.MINIMAL;
        }
        if (text.contains("클래식") || text.contains("classic")) {
            return StyleMode.CLASSIC;
        }
        if (text.contains("스포티") || text.contains("sporty")) {
            return StyleMode.SPORTY;
        }
        if (text.contains("스트릿") || text.contains("street")) {
            return StyleMode.STREET;
        }
        if (text.contains("화려") || text.contains("glam")) {
            return StyleMode.GLAM;
        }
        return StyleMode.COMFORTABLE;
    }

    // 온도를 계절 값으로 변환한다.
    private SeasonType inferSeason(double temp) {
        if (temp >= 25) {
            return SeasonType.SUMMER;
        }
        if (temp >= 17) {
            return SeasonType.SPRING;
        }
        if (temp >= 8) {
            return SeasonType.FALL;
        }
        return SeasonType.WINTER;
    }
}
