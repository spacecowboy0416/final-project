package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.SeasonType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Gemini blueprint 출력 JSON 계약 DTO.
 */
public record BlueprintOutputDto(
    @JsonProperty("ai_blueprint")
    @Valid @NotNull
    AiBlueprint aiBlueprint
) {
    public record AiBlueprint(
        @NotNull
        TpoType tpoType,
        @NotNull
        StyleType styleType,
        @JsonProperty("main_item_analysis")
        @Valid @NotNull
        MainItemAnalysis mainItemAnalysis,
        @Valid @NotNull
        Coordination coordination,
        @JsonProperty("styling_rule_applied")
        @NotBlank
        String stylingRuleApplied
    ) {
    }

    public record MainItemAnalysis(
        @NotBlank
        String temp,
        @NotNull
        SeasonType season,
        @NotNull
        ColorType color,
        @NotNull
        ItemCategoryType type,
        @NotNull
        StyleType style
    ) {
    }

    public record Coordination(
        @Valid @NotNull
        CoordinationSlot tops,
        @Valid @NotNull
        CoordinationSlot bottoms,
        @Valid @NotNull
        CoordinationSlot outerwear,
        @Valid @NotNull
        CoordinationSlot shoes,
        @Valid @NotNull
        CoordinationSlot accessories
    ) {
    }

    public record CoordinationSlot(
        @JsonProperty("item_name")
        @NotBlank
        String itemName,
        @NotNull
        ItemCategoryType category,
        @JsonProperty("attributes")
        @Valid @NotNull
        Attributes attributes,
        @JsonProperty("temp_range")
        @NotNull @Size(min = 2, max = 2)
        List<@NotNull Integer> tempRange,
        @NotBlank
        String reasoning,
        @NotNull
        PriorityType priority
    ) {
    }

    public record Attributes(
        @NotNull
        ColorType color,
        @NotNull
        MaterialType material,
        @NotNull
        FitType fit,
        @NotNull
        StyleType style
    ) {
    }
}
