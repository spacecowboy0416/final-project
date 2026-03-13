package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.internal.BlueprintValidationDto;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BlueprintResponseBuilder {
    /**
     * 검증된 blueprint 출력 DTO를 기반으로 최종 coordination 응답 DTO를 만든다.
     * TODO: 현재는 blueprint 값을 그대로 응답에 옮기는 임시 빌더다.
     * TODO: matchedItemsBySlot, weather를 활용한 실제 최종 응답 조립은 추후 구현한다.
     */
    public CoordinationOutputDto build(
        BlueprintValidationDto.ValidatedBlueprint validatedBlueprint,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot,
        WeatherFetcher.WeatherContext weather
    ) {
        BlueprintOutputDto.AiBlueprint aiBlueprint = validatedBlueprint.aiBlueprint();
        return new CoordinationOutputDto(
            null,
            aiBlueprint.tpoType(),
            aiBlueprint.styleType(),
            aiBlueprint.stylingRuleApplied(),
            buildBlueprintOutputs(validatedBlueprint)
        );
    }

    private List<CoordinationItemOutputDto> buildBlueprintOutputs(BlueprintValidationDto.ValidatedBlueprint validatedBlueprint) {
        // TODO: 현재는 매칭 결과가 아니라 blueprint slot 원본만 응답용 DTO로 변환한다.
        return Arrays.stream(CategoryType.values())
            .map(categoryType -> toBlueprintOutput(validatedBlueprint.slotsByCategory().get(categoryType)))
            .toList();
    }

    private CoordinationItemOutputDto toBlueprintOutput(BlueprintValidationDto.Slot slot) {
        if (slot == null || slot.raw() == null) {
            return new CoordinationItemOutputDto(
                slot == null ? null : slot.slotKey(),
                null,
                null,
                null,
                0.0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
        }

        BlueprintOutputDto.CoordinationSlot slotData = slot.raw();
        BlueprintOutputDto.Attributes attributes = slotData.attributes();
        // TODO: imageUrl, matchScore는 실제 상품 매칭 결과 반영 전까지 임시값(null/0.0)으로 둔다.
        return new CoordinationItemOutputDto(
            slot.slotKey(),
            slotData.itemName(),
            null,
            slotData.category(),
            0.0,
            readTempRangeValue(slotData.tempRange(), 0),
            readTempRangeValue(slotData.tempRange(), 1),
            slotData.priority(),
            slotData.reasoning(),
            attributes == null ? null : attributes.color(),
            attributes == null ? null : attributes.material(),
            attributes == null ? null : attributes.fit(),
            attributes == null ? null : attributes.style()
        );
    }

    private Integer readTempRangeValue(List<Integer> tempRange, int index) {
        if (tempRange == null || tempRange.size() <= index) {
            return null;
        }
        return tempRange.get(index);
    }
}
