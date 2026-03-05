package com.finalproject.coordi.recommendation.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.DraftRecommendationItem;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NormalizationService {
    // DRAFT 아이템 속성을 내부 표준값으로 정규화한다.
    public DraftRecommendationItem normalize(DraftRecommendationItem draft, ObjectNode styleVector) {
        Map<String, Object> normalizedAttributes = new HashMap<>(draft.attributes());
        String rawColor = String.valueOf(normalizedAttributes.getOrDefault("color", "unknown"));
        String normalizedColor = normalizeColor(rawColor);
        normalizedAttributes.put("color", normalizedColor);
        normalizedAttributes.put("color_code", colorHex(normalizedColor));
        normalizedAttributes.put("style_vector", styleVector);

        return new DraftRecommendationItem(
            draft.slotKey(),
            draft.itemName(),
            draft.searchQuery(),
            draft.category(),
            normalizedAttributes,
            draft.tempMin(),
            draft.tempMax(),
            draft.reasoning(),
            draft.priority()
        );
    }

    // 스타일 텍스트를 3차원 벡터로 변환한다.
    public ObjectNode buildStyleVector(String style, ObjectNode node) {
        node.put("minimal", "minimal".equals(style) ? 0.9 : 0.4);
        node.put("sporty", "sporty".equals(style) ? 0.9 : 0.4);
        node.put("classic", "classic".equals(style) ? 0.9 : 0.4);
        return node;
    }

    // 색상 표현을 표준 코드로 정규화한다.
    private String normalizeColor(String rawColor) {
        String value = rawColor.toLowerCase(Locale.ROOT).trim();
        if (value.contains("진청") || value.contains("네이비") || value.contains("남색")) {
            return "navy";
        }
        if (value.contains("회색") || value.contains("그레이")) {
            return "gray";
        }
        if (value.contains("검정") || value.contains("블랙")) {
            return "black";
        }
        if (value.contains("흰") || value.contains("화이트")) {
            return "white";
        }
        return "unknown";
    }

    // 표준 색상명에 대응하는 hex 값을 반환한다.
    private String colorHex(String color) {
        return switch (color) {
            case "navy" -> "#1F2A44";
            case "gray" -> "#808080";
            case "black" -> "#111111";
            case "white" -> "#FFFFFF";
            default -> "#999999";
        };
    }
}
