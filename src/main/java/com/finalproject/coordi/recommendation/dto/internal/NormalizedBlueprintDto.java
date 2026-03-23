package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

// blueprint 생성 후, 검증을 거치고 슬롯별 아이템 정보가 매핑된 상태의 DTO
public record NormalizedBlueprintDto(
    RawBlueprintDto rawBlueprint,
    Map<CategoryType, RawBlueprintDto.ItemInfo> itemsBySlot
) {
    public NormalizedBlueprintDto {
        rawBlueprint = Objects.requireNonNull(rawBlueprint, "rawBlueprint must not be null.");
        Map<CategoryType, RawBlueprintDto.ItemInfo> sourceItemsBySlot = Objects.requireNonNull(
            itemsBySlot,
            "itemsBySlot must not be null."
        );

        EnumMap<CategoryType, RawBlueprintDto.ItemInfo> copied = new EnumMap<>(CategoryType.class);
        copied.putAll(sourceItemsBySlot);
        itemsBySlot = Collections.unmodifiableMap(copied);
    }

    public RawBlueprintDto.AiBlueprint aiBlueprint() {
        return rawBlueprint.aiBlueprint();
    }

    public CategoryType anchorSlot() {
        return aiBlueprint().anchorSlot();
    }

    public RawBlueprintDto.ItemInfo itemBySlot(CategoryType slotKey) {
        return itemsBySlot.get(slotKey);
    }
}
