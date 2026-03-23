package com.finalproject.coordi.recommendation.service.blueprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.BrandType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.SeasonType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlueprintNormalizerTest {
    private final BlueprintNormalizer normalizer = new BlueprintNormalizer();

    @Test
    void 유효한Blueprint를_슬롯맵으로정규화한다() {
        RawBlueprintDto rawBlueprint = validRawBlueprint(
            new RawBlueprintDto.Coordination(
                validItem(CategoryType.HEADWEAR, GenderType.UNISEX),
                validItem(CategoryType.TOPS, null),
                validItem(CategoryType.BOTTOMS, GenderType.FEMALE),
                validItem(CategoryType.OUTERWEAR, GenderType.FEMALE),
                validItem(CategoryType.SHOES, GenderType.FEMALE),
                validItem(CategoryType.ACCESSORIES, GenderType.UNISEX)
            )
        );

        NormalizedBlueprintDto result = normalizer.normalize(rawBlueprint);

        assertNotNull(result.itemsBySlot().get(CategoryType.TOPS));
        assertEquals(GenderType.FEMALE, result.itemBySlot(CategoryType.TOPS).attributes().gender());
        assertEquals(CategoryType.TOPS, result.itemBySlot(CategoryType.TOPS).slotKey());
        assertEquals(EnumSet.allOf(CategoryType.class), result.itemsBySlot().keySet());
    }

    @Test
    void 선택슬롯이없어도_정규화에성공한다() {
        RawBlueprintDto rawBlueprint = validRawBlueprint(
            new RawBlueprintDto.Coordination(
                null,
                validItem(CategoryType.TOPS, GenderType.FEMALE),
                validItem(CategoryType.BOTTOMS, GenderType.FEMALE),
                validItem(CategoryType.OUTERWEAR, GenderType.FEMALE),
                validItem(CategoryType.SHOES, GenderType.FEMALE),
                null
            )
        );

        NormalizedBlueprintDto result = normalizer.normalize(rawBlueprint);

        assertNull(result.itemBySlot(CategoryType.HEADWEAR));
        assertNull(result.itemBySlot(CategoryType.ACCESSORIES));
        assertNotNull(result.itemBySlot(CategoryType.TOPS));
        assertEquals(6, result.itemsBySlot().size());
        assertEquals(EnumSet.allOf(CategoryType.class), result.itemsBySlot().keySet());
    }

    private RawBlueprintDto validRawBlueprint(RawBlueprintDto.Coordination coordination) {
        return new RawBlueprintDto(
            new RawBlueprintDto.AiBlueprint(
                GenderType.FEMALE,
                TpoType.DAILY,
                StyleType.CASUAL,
                CategoryType.TOPS,
                new RawBlueprintDto.MainItemAnalysis(
                    "mild",
                    SeasonType.SPRING,
                    ColorType.BLACK,
                    ItemCategoryType.SHIRT,
                    StyleType.CASUAL
                ),
                coordination,
                "오늘 날씨와 분위기에 맞는 추천입니다."
            )
        );
    }

    private RawBlueprintDto.ItemInfo validItem(CategoryType slotKey, GenderType itemGender) {
        return new RawBlueprintDto.ItemInfo(
            slotKey,
            slotKey.getKeyword() + " 아이템",
            ItemCategoryType.SHIRT,
            new RawBlueprintDto.Attributes(
                itemGender,
                ColorType.BLACK,
                MaterialType.COTTON,
                FitType.STANDARD,
                BrandType.UNIQLO,
                StyleType.CASUAL
            ),
            List.of(10, 20),
            "추천 이유",
            PriorityType.ESSENTIAL
        );
    }
}
