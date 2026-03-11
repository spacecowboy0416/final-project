package com.finalproject.coordi.recommendation.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.internal.CoordinationDto;
import com.finalproject.coordi.recommendation.dto.internal.CoordinationItemDto;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class BlueprintResponseBuilder {
    private static final Set<String> NEUTRAL_COLOR_CODES = Set.of(
        "black", "white", "gray", "navy", "beige", "brown", "khaki"
    );

    /**
     * Gemini blueprint에서 슬롯과 메타를 분리해, 중복 없이 최종 recommendation 응답 DTO를 만든다.
     */
    public CoordinationOutputDto build(
        Object validatedBlueprint,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot,
        WeatherFetcher.WeatherContext weather
    ) {
        if (!(validatedBlueprint instanceof JsonNode rawBlueprintJson)) {
            throw new IllegalStateException("검증된 blueprint 타입이 JsonNode가 아닙니다.");
        }

        JsonNode aiBlueprint = rawBlueprintJson.path("ai_blueprint");
        JsonNode coordination = aiBlueprint.path("coordination");
        String stylingRuleApplied = aiBlueprint.path("styling_rule_applied").asText(null);

        CoordinationDto internalOutput = new CoordinationDto(
            null,
            parseTpoType(aiBlueprint.path("tpoType").asText(null)),
            parseStyleType(aiBlueprint.path("styleType").asText(null)),
            stylingRuleApplied,
            buildBlueprintOutputs(coordination, aiBlueprint.path("main_item_analysis"), stylingRuleApplied, weather, matchedItemsBySlot)
        );

        return toApiOutput(internalOutput);
    }

    private CoordinationOutputDto toApiOutput(CoordinationDto internalOutput) {
        return new CoordinationOutputDto(
            internalOutput.blueprintId(),
            internalOutput.tpoType(),
            internalOutput.styleType(),
            internalOutput.stylingRuleApplied(),
            internalOutput.items().stream()
                .map(slot -> new BlueprintOutputDto(
                    slot.slotKey(),
                    slot.itemName(),
                    slot.imageUrl(),
                    slot.category(),
                    slot.matchScore(),
                    slot.tempMin(),
                    slot.tempMax(),
                    slot.priority(),
                    slot.reasoning(),
                    slot.color(),
                    slot.material(),
                    slot.fit(),
                    slot.style()
                ))
                .toList()
        );
    }

    private List<CoordinationItemDto> buildBlueprintOutputs(
        JsonNode coordination,
        JsonNode mainItemAnalysis,
        String stylingRuleApplied,
        WeatherFetcher.WeatherContext weather,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot
    ) {
        return Arrays.stream(CategoryType.values())
            .map(categoryType -> toBlueprintOutput(
                categoryType,
                coordination.path(categoryType.code()),
                mainItemAnalysis,
                stylingRuleApplied,
                weather,
                matchedItemsBySlot.get(categoryType)
            ))
            .toList();
    }

    private CoordinationItemDto toBlueprintOutput(
        CategoryType categoryType,
        JsonNode slotNode,
        JsonNode mainItemAnalysis,
        String stylingRuleApplied,
        WeatherFetcher.WeatherContext weather,
        ItemMatcher.MatchedItem matchedItem
    ) {
        JsonNode attributesNode = slotNode.path("attributes");
        double styleScore = calculateStyleScore(attributesNode, mainItemAnalysis, stylingRuleApplied);
        double colorScore = calculateColorScore(attributesNode, mainItemAnalysis);
        double tempScore = calculateTempScore(slotNode.path("temp_range"), weather);
        double matchScore = calculateMatchScore(styleScore, colorScore, tempScore, slotNode.path("priority").asText(null));

        return new CoordinationItemDto(
            categoryType,
            slotNode.path("item_name").asText(null),
            extractImageUrl(matchedItem),
            parseItemCategoryType(slotNode.path("category").asText(null)),
            matchScore,
            readTempRangeValue(slotNode.path("temp_range"), 0),
            readTempRangeValue(slotNode.path("temp_range"), 1),
            parsePriorityType(slotNode.path("priority").asText(null)),
            slotNode.path("reasoning").asText(null),
            parseColorType(attributesNode.path("color").asText(null)),
            parseMaterialType(attributesNode.path("material").asText(null)),
            parseFitType(attributesNode.path("fit").asText(null)),
            parseStyleType(attributesNode.path("style").asText(null))
        );
    }

    private String extractImageUrl(ItemMatcher.MatchedItem matchedItem) {
        if (matchedItem == null || matchedItem.product() == null) {
            return null;
        }
        return matchedItem.product().productImageUrl();
    }

    private double calculateMatchScore(double styleScore, double colorScore, double tempScore, String rawPriority) {
        double priorityWeight = switch (safeLower(rawPriority)) {
            case "essential" -> 1.0;
            case "optional" -> 0.9;
            default -> 0.95;
        };
        double weightedScore = (styleScore * 0.40) + (colorScore * 0.25) + (tempScore * 0.35);
        return roundScore(weightedScore * priorityWeight);
    }

    private double calculateStyleScore(JsonNode attributesNode, JsonNode mainItemAnalysis, String stylingRuleApplied) {
        String slotStyle = safeLower(attributesNode.path("style").asText(null));
        String mainStyle = safeLower(mainItemAnalysis.path("style").asText(null));
        String stylingRule = safeLower(stylingRuleApplied);
        if (slotStyle == null || mainStyle == null) {
            return 0.55;
        }
        if (slotStyle.equals(mainStyle)) {
            return 1.0;
        }
        if (stylingRule != null && (stylingRule.contains(slotStyle) || stylingRule.contains(mainStyle))) {
            return 0.82;
        }
        return 0.65;
    }

    private double calculateColorScore(JsonNode attributesNode, JsonNode mainItemAnalysis) {
        String slotColor = safeLower(attributesNode.path("color").asText(null));
        String mainColor = safeLower(mainItemAnalysis.path("color").asText(null));
        if (slotColor == null || mainColor == null) {
            return 0.55;
        }
        if (slotColor.equals(mainColor)) {
            return 1.0;
        }
        if (NEUTRAL_COLOR_CODES.contains(slotColor) || NEUTRAL_COLOR_CODES.contains(mainColor)) {
            return 0.88;
        }
        if (shareColorFamily(slotColor, mainColor)) {
            return 0.76;
        }
        return 0.60;
    }

    private boolean shareColorFamily(String leftColor, String rightColor) {
        return sameFamily(leftColor, rightColor, "blue", "navy")
            || sameFamily(leftColor, rightColor, "beige", "brown", "khaki")
            || sameFamily(leftColor, rightColor, "red", "pink", "orange")
            || sameFamily(leftColor, rightColor, "green", "khaki")
            || sameFamily(leftColor, rightColor, "black", "white", "gray", "navy");
    }

    private boolean sameFamily(String leftColor, String rightColor, String... family) {
        Set<String> familyCodes = Set.of(family);
        return familyCodes.contains(leftColor) && familyCodes.contains(rightColor);
    }

    private double calculateTempScore(JsonNode tempRangeNode, WeatherFetcher.WeatherContext weather) {
        Integer minTemp = readTempRangeValue(tempRangeNode, 0);
        Integer maxTemp = readTempRangeValue(tempRangeNode, 1);
        Double referenceTemp = weather.feelsLike() != null ? weather.feelsLike() : weather.temperature();
        if (minTemp == null || maxTemp == null || referenceTemp == null) {
            return 0.5;
        }

        double targetTemp = referenceTemp;
        if (targetTemp >= minTemp && targetTemp <= maxTemp) {
            return 1.0;
        }

        double distance = targetTemp < minTemp ? minTemp - targetTemp : targetTemp - maxTemp;
        if (distance <= 3.0) {
            return 0.8;
        }
        if (distance <= 6.0) {
            return 0.65;
        }
        if (distance <= 10.0) {
            return 0.45;
        }
        return 0.25;
    }

    private Integer readTempRangeValue(JsonNode tempRangeNode, int index) {
        if (!tempRangeNode.isArray() || tempRangeNode.size() <= index || !tempRangeNode.path(index).canConvertToInt()) {
            return null;
        }
        return tempRangeNode.path(index).asInt();
    }

    private double roundScore(double score) {
        return Math.round(score * 10000.0) / 10000.0;
    }

    private String safeLower(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase();
    }

    private TpoType parseTpoType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return TpoType.fromCode(rawCode);
    }

    private StyleType parseStyleType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return StyleType.fromCode(rawCode);
    }

    private ColorType parseColorType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return ColorType.fromCode(rawCode);
    }

    private MaterialType parseMaterialType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return MaterialType.fromCode(rawCode);
    }

    private FitType parseFitType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return FitType.fromCode(rawCode);
    }

    private ItemCategoryType parseItemCategoryType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return ItemCategoryType.fromCode(rawCode);
    }

    private PriorityType parsePriorityType(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }
        return PriorityType.fromCode(rawCode);
    }
}
