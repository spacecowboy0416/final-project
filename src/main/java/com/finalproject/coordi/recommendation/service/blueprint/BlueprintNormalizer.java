package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import java.util.Arrays;
import java.util.EnumMap;
import org.springframework.stereotype.Component;

@Component
public class BlueprintNormalizer {

    public NormalizedBlueprintDto normalize(RawBlueprintDto rawBlueprint) {
        RawBlueprintDto.Coordination coordination = rawBlueprint.aiBlueprint().coordination();
        EnumMap<CategoryType, RawBlueprintDto.ItemInfo> itemsBySlot = new EnumMap<>(CategoryType.class);

        // 모든 슬롯을 초기화
        Arrays.stream(CategoryType.values()).forEach(categoryType -> itemsBySlot.put(categoryType, null));

        // DTO 내부 규칙으로 성별 보정을 수행하고 슬롯 맵을 구성한다.
        coordination.getAllItems().stream()
            .map(item -> item.withFallbackGender(rawBlueprint.aiBlueprint().gender()))
            .forEach(item -> itemsBySlot.put(item.slotKey(), item));

        return new NormalizedBlueprintDto(rawBlueprint, itemsBySlot);
    }
}
