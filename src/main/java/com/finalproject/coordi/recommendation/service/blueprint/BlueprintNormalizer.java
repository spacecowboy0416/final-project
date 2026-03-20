package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class BlueprintNormalizer {

    public NormalizedBlueprintDto normalize(RawBlueprintDto rawBlueprint) {
        // 1. 전체 데이터나 AI 응답 본문이 null인 경우 안전하게 빈 결과 반환
        if (rawBlueprint == null || rawBlueprint.aiBlueprint() == null) {
            return new NormalizedBlueprintDto(rawBlueprint, new EnumMap<>(CategoryType.class));
        }

        RawBlueprintDto.Coordination coordination = rawBlueprint.aiBlueprint().coordination();
        EnumMap<CategoryType, RawBlueprintDto.ItemInfo> itemsBySlot = new EnumMap<>(CategoryType.class);

        // 모든 슬롯을 초기화
        Arrays.stream(CategoryType.values()).forEach(categoryType -> itemsBySlot.put(categoryType, null));

        // 2. 아이템 정보들을 스트림으로 변환하여 안전하게 처리
        itemInfos(coordination).stream()
            .filter(item -> item != null && item.slotKey() != null)
            .map(item -> normalizeItem(item, rawBlueprint.aiBlueprint().gender()))
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

    /**
     * AI 응답 객체에서 개별 아이템들을 추출하여 리스트로 반환합니다.
     * List.of() 대신 Stream을 사용하여 null 값이 포함되어도 안전하게 필터링합니다.
     */
    private List<RawBlueprintDto.ItemInfo> itemInfos(RawBlueprintDto.Coordination coordination) {
        if (coordination == null) {
            return List.of();
        }

        return Stream.of(
                coordination.headwear(),
                coordination.tops(),
                coordination.bottoms(),
                coordination.outerwear(),
                coordination.shoes(),
                coordination.accessories()
            )
            .filter(Objects::nonNull) // null인 요소는 리스트에 담지 않음 (NPE 방지)
            .toList(); 
    }
}