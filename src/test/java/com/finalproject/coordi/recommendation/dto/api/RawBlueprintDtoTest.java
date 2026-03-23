package com.finalproject.coordi.recommendation.dto.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.BrandType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import java.util.List;
import org.junit.jupiter.api.Test;

class RawBlueprintDtoTest {

    @Test
    void getAllItems는_null이아닌아이템만_지정순서로반환한다() {
        RawBlueprintDto.Coordination coordination = new RawBlueprintDto.Coordination(
            null,
            validItem(CategoryType.TOPS, GenderType.UNISEX),
            validItem(CategoryType.BOTTOMS, GenderType.UNISEX),
            validItem(CategoryType.OUTERWEAR, GenderType.UNISEX),
            validItem(CategoryType.SHOES, GenderType.UNISEX),
            validItem(CategoryType.ACCESSORIES, GenderType.UNISEX)
        );

        List<RawBlueprintDto.ItemInfo> allItems = coordination.getAllItems();

        assertEquals(5, allItems.size());
        assertEquals(CategoryType.TOPS, allItems.get(0).slotKey());
        assertEquals(CategoryType.BOTTOMS, allItems.get(1).slotKey());
        assertEquals(CategoryType.OUTERWEAR, allItems.get(2).slotKey());
        assertEquals(CategoryType.SHOES, allItems.get(3).slotKey());
        assertEquals(CategoryType.ACCESSORIES, allItems.get(4).slotKey());
    }

    @Test
    void withGender는_성별만교체한_새Attributes를반환한다() {
        RawBlueprintDto.Attributes attributes = new RawBlueprintDto.Attributes(
            null,
            ColorType.BLACK,
            MaterialType.COTTON,
            FitType.STANDARD,
            BrandType.UNIQLO,
            StyleType.CASUAL
        );

        RawBlueprintDto.Attributes changed = attributes.withGender(GenderType.FEMALE);

        assertNotSame(attributes, changed);
        assertEquals(GenderType.FEMALE, changed.gender());
        assertEquals(attributes.color(), changed.color());
        assertEquals(attributes.material(), changed.material());
        assertEquals(attributes.fit(), changed.fit());
        assertEquals(attributes.brand(), changed.brand());
        assertEquals(attributes.style(), changed.style());
    }

    @Test
    void withFallbackGender는_성별이없을때만_fallback을적용한다() {
        RawBlueprintDto.ItemInfo item = validItem(CategoryType.TOPS, null);

        RawBlueprintDto.ItemInfo normalized = item.withFallbackGender(GenderType.MALE);

        assertNotSame(item, normalized);
        assertNull(item.attributes().gender());
        assertEquals(GenderType.MALE, normalized.attributes().gender());
        assertEquals(item.slotKey(), normalized.slotKey());
        assertEquals(item.itemName(), normalized.itemName());
        assertEquals(item.category(), normalized.category());
        assertEquals(item.tempRange(), normalized.tempRange());
        assertEquals(item.reasoning(), normalized.reasoning());
        assertEquals(item.priority(), normalized.priority());
    }

    @Test
    void withFallbackGender는_이미성별이있으면_원본객체를반환한다() {
        RawBlueprintDto.ItemInfo item = validItem(CategoryType.TOPS, GenderType.FEMALE);

        RawBlueprintDto.ItemInfo normalized = item.withFallbackGender(GenderType.MALE);

        assertSame(item, normalized);
        assertEquals(GenderType.FEMALE, normalized.attributes().gender());
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
