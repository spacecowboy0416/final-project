package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BlueprintNormalizer {
    public NormalizedBlueprintDto normalize(RawBlueprintDto rawBlueprint) {
        RawBlueprintDto.Coordination coordination = rawBlueprint.aiBlueprint().coordination();
        EnumMap<CategoryType, RawBlueprintDto.ItemInfo> itemsBySlot = new EnumMap<>(CategoryType.class);

        Arrays.stream(CategoryType.values()).forEach(categoryType -> itemsBySlot.put(categoryType, null));

        itemInfos(coordination).stream()
            .filter(item -> item != null && item.slotKey() != null)
            .map(item -> normalizeItem(item, rawBlueprint.aiBlueprint() == null ? null : rawBlueprint.aiBlueprint().gender()))
            .forEach(item -> itemsBySlot.put(item.slotKey(), item));

        return new NormalizedBlueprintDto(rawBlueprint, itemsBySlot);
    }

    private RawBlueprintDto.ItemInfo normalizeItem(
        RawBlueprintDto.ItemInfo item,
        GenderType fallbackGender
    ) {
        if (item == null || item.attributes() == null) {
            return item;
        }

        RawBlueprintDto.Attributes attributes = item.attributes();
        if (attributes.gender() != null) {
            return item;
        }

        RawBlueprintDto.Attributes normalizedAttributes = new RawBlueprintDto.Attributes(
            fallbackGender,
            attributes.color(),
            attributes.material(),
            attributes.fit(),
            attributes.brand(),
            attributes.pattern(),
            attributes.style()
        );

        return new RawBlueprintDto.ItemInfo(
            item.slotKey(),
            item.itemName(),
            item.category(),
            normalizedAttributes,
            item.tempRange(),
            item.reasoning(),
            item.priority()
        );
    }

    private List<RawBlueprintDto.ItemInfo> itemInfos(RawBlueprintDto.Coordination coordination) {
        return List.of(
            coordination.headwear(),
            coordination.tops(),
            coordination.bottoms(),
            coordination.outerwear(),
            coordination.shoes(),
            coordination.accessories()
        );
    }
}
