package com.finalproject.coordi.recommendation.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * AI 출력 계약(DTO + 필드 제약)을 한 클래스에 모아 관리한다.
 */
public final class AiBlueprintConstraintDto {
    private AiBlueprintConstraintDto() {
    }

    public record Envelope(
        @NotNull
        @Valid
        @JsonProperty("ai_blueprint")
        Blueprint aiBlueprint
    ) {
    }

    public record Blueprint(
        @NotBlank
        String schemaVersion,
        @NotNull
        @Valid
        @JsonProperty("main_item_analysis")
        MainItemAnalysis mainItemAnalysis,
        @NotNull
        @Valid
        Map<String, Slot> coordination,
        @NotBlank
        @JsonProperty("styling_rule_applied")
        String stylingRuleApplied
    ) {
        @AssertTrue(message = "coordination 필수 슬롯이 누락되었습니다.")
        public boolean hasAllRequiredSlots() {
            if (coordination == null) {
                return false;
            }
            for (SlotKey slotKey : SlotKey.values()) {
                if (!coordination.containsKey(slotKey.code())) {
                    return false;
                }
            }
            return true;
        }
    }

    public record MainItemAnalysis(
        @NotBlank String temp,
        @NotBlank String season,
        @NotBlank String color,
        @NotBlank String type,
        @NotBlank String style
    ) {
    }

    public record Slot(
        @NotBlank
        @JsonProperty("item_name")
        String itemName,
        @NotBlank
        @JsonProperty("search_query")
        String searchQuery,
        @NotBlank
        String category,
        @NotNull
        @Valid
        Attributes attributes,
        @NotNull
        @Size(min = 2, max = 2)
        @JsonProperty("temp_range")
        List<@NotNull Integer> tempRange,
        @NotBlank
        String reasoning,
        @NotBlank
        @Pattern(regexp = "^(?i)(essential|optional)$")
        String priority
    ) {
    }

    public record Attributes(
        @NotBlank String color,
        @NotBlank String material,
        @NotBlank String fit,
        @NotBlank String style
    ) {
    }
}
