package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;

/**
 * 제약(스키마/필수값) 검증을 통과한 AI blueprint에서 슬롯별로 추출한 내부 작업용 DTO.
 */
public record BlueprintSlot(
    SlotKey slotKey,
    String itemName,
    String searchQuery,
    String reasoning,
    PriorityType priority,
    String color,
    String material,
    String fit,
    String style
) {
    public static BlueprintSlot from(SlotKey slotKey, AiBlueprintConstraintDto.Slot slot) {
        var attributes = slot.attributes();
        return new BlueprintSlot(
            slotKey,
            slot.itemName(),
            slot.searchQuery(),
            slot.reasoning(),
            PriorityType.fromCode(slot.priority()),
            attributes.color(),
            attributes.material(),
            attributes.fit(),
            attributes.style()
        );
    }
}
