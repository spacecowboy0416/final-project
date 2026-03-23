package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 추천 저장 버튼 클릭 시 전달하는 최소 저장 요청 계약 DTO.
 */
public record RecommendationSaveRequestDto(
    @NotBlank
    String naturalText,
    @Valid @NotNull
    UserRequestDto.WeatherInput weather,
    @NotNull
    TpoType tpoType,
    @NotNull
    StyleType styleType,
    String aiExplanation,
    @NotEmpty
    List<@Valid @NotNull CoordinationItemOutputDto> coordination,
    @NotNull
    Map<String, String> queryMap
) {
    // 저장 로직에서 반복되는 null 방어를 DTO 내부로 모은다.
    public String weatherStatusCode() {
        if (weather == null || weather.status() == null) {
            return null;
        }
        return weather.status().getCode();
    }

    public String aiExplanationOrEmpty() {
        return aiExplanation == null ? "" : aiExplanation;
    }

    public List<CoordinationItemOutputDto> persistableCoordination() {
        if (coordination == null || coordination.isEmpty()) {
            return List.of();
        }
        return coordination.stream()
            .filter(item -> item != null && item.slotKey() != null)
            .toList();
    }

    public String searchQueryOf(CategoryType slotKey) {
        if (queryMap == null || queryMap.isEmpty() || slotKey == null) {
            return "";
        }
        String byCode = queryMap.get(slotKey.getCode());
        if (byCode != null) {
            return byCode;
        }
        String byName = queryMap.get(slotKey.name());
        return byName == null ? "" : byName;
    }
}
