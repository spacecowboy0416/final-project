package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import java.util.Map;

// blueprint 생성 후, 검증을 거치고 슬롯별 아이템 정보가 매핑된 상태의 DTO
public record NormalizedBlueprintDto(
    RawBlueprintDto rawBlueprint,
    Map<CategoryType, RawBlueprintDto.ItemInfo> itemsBySlot
) {
    public RawBlueprintDto.AiBlueprint aiBlueprint() {
        return rawBlueprint == null ? null : rawBlueprint.aiBlueprint();
    }

    public RawBlueprintDto.ItemInfo itemBySlot(CategoryType slotKey) {
        return slotKey == null || itemsBySlot == null ? null : itemsBySlot.get(slotKey);
    }
}
