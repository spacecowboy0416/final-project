package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.TpoType;
import org.springframework.stereotype.Component;
import java.util.Locale;

/**
 * AI의 모호한 응답이나 텍스트를 시스템 표준 Enum으로 변환하는 전용 컴포넌트
 */
@Component
public class AttributeNormalizer {

    public TpoType normalizeTpo(String rawTpo) {
        if (rawTpo == null) return TpoType.CASUAL;
        
        // Java 21: Switch Expression을 사용한 깔끔한 매핑
        return switch (rawTpo.toLowerCase(Locale.ROOT)) {
            case String s when s.contains("데이트") || s.contains("date") -> TpoType.DATE;
            case String s when s.contains("출근") || s.contains("work") -> TpoType.WORK;
            case String s when s.contains("운동") || s.contains("exercise") -> TpoType.EXERCISE;
            case String s when s.contains("여행") || s.contains("travel") -> TpoType.TRAVEL;
            case String s when s.contains("격식") || s.contains("formal") -> TpoType.FORMAL;
            default -> TpoType.CASUAL;
        };
    }

    public StyleMode normalizeStyle(String rawMood) {
        if (rawMood == null) return StyleMode.COMFORTABLE;

        return switch (rawMood.toLowerCase(Locale.ROOT)) {
            case String s when s.contains("미니멀") || s.contains("minimal") -> StyleMode.MINIMAL;
            case String s when s.contains("클래식") || s.contains("classic") -> StyleMode.CLASSIC;
            case String s when s.contains("스포티") || s.contains("sporty") -> StyleMode.SPORTY;
            case String s when s.contains("스트릿") || s.contains("street") -> StyleMode.STREET;
            case String s when s.contains("글램") || s.contains("glam") -> StyleMode.GLAM;
            default -> StyleMode.COMFORTABLE;
        };
    }
}