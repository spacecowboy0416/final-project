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
public class CoordinationBuilder {
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
            buildCoordinationOutputs(validatedBlueprint, matchedItemsBySlot)
        );
    }

    private List<CoordinationItemOutputDto> buildCoordinationOutputs(
        BlueprintValidationDto.ValidatedBlueprint validatedBlueprint,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot
    ) {
        return Arrays.stream(CategoryType.values())
            .map(categoryType -> toCoordinationOutput(
                validatedBlueprint.slotsByCategory().get(categoryType),
                matchedItemsBySlot == null ? null : matchedItemsBySlot.get(categoryType)
            ))
            .toList();
    }

    private CoordinationItemOutputDto toCoordinationOutput(
        BlueprintValidationDto.Slot slot,
        ItemMatcher.MatchedItem matchedItem
    ) {
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
        var matchedProduct = matchedItem == null ? null : matchedItem.product();
        String itemName = matchedProduct != null && matchedProduct.productName() != null && !matchedProduct.productName().isBlank()
            ? matchedProduct.productName()
            : slotData.itemName();
        String imageUrl = matchedProduct == null ? null : matchedProduct.productImageUrl();
        double matchScore = matchedItem == null ? 0.0 : matchedItem.matchScore();
        return new CoordinationItemOutputDto(
            slot.slotKey(),
            itemName,
            imageUrl,
            slotData.category(),
            matchScore,
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
