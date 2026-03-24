package com.finalproject.coordi.recommendation.dto.api;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.BrandType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.SeasonType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 외부 AI 응답의 원본 경계 DTO
 */
public record RawBlueprintDto(
    @JsonProperty("ai_blueprint")
    @Valid @NotNull
    AiBlueprint aiBlueprint
) {
    public record AiBlueprint(
        @NotNull
        GenderType gender,
        @NotNull
        TpoType tpoType,
        @NotNull
        StyleType styleType,
        @JsonProperty("anchor_slot")
        @NotNull
        CategoryType anchorSlot,
        @JsonProperty("main_item_analysis")
        @Valid @NotNull
        MainItemAnalysis mainItemAnalysis,
        @Valid @NotNull
        Coordination coordination,
        @JsonProperty("ai_explanation")
        @NotBlank
        String aiExplanation
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
        @Valid
        ItemInfo headwear,
        @Valid @NotNull
        ItemInfo tops,
        @Valid @NotNull
        ItemInfo bottoms,
        @Valid @NotNull
        ItemInfo outerwear,
        @Valid @NotNull
        ItemInfo shoes,
        @Valid
        ItemInfo accessories
    ) {
        public List<ItemInfo> getAllItems() {
            return Arrays.stream(new ItemInfo[]{tops, bottoms, outerwear, shoes, headwear, accessories})
                .filter(Objects::nonNull)
                .toList();
        }
    }

    public record ItemInfo(
        @JsonProperty("slot_key")
        @NotNull
        CategoryType slotKey,
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
        public ItemInfo withFallbackGender(GenderType fallbackGender) {
            if (attributes == null || attributes.gender() != null) {
                return this;
            }

            return new ItemInfo(
                slotKey,
                itemName,
                category,
                attributes.withGender(fallbackGender),
                tempRange,
                reasoning,
                priority
            );
        }
    }

    public record Attributes(
        GenderType gender,
        @NotNull
        ColorType color,
        @NotNull
        MaterialType material,
        @NotNull
        FitType fit,
        BrandType brand,
        @NotNull
        StyleType style
    ) {
        public Attributes withGender(GenderType gender) {
            return new Attributes(
                gender,
                color,
                material,
                fit,
                brand,
                style
            );
        }
    }
}
