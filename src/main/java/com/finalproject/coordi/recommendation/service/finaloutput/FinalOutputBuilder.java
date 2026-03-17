package com.finalproject.coordi.recommendation.service.finaloutput;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.service.coordination.ItemMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FinalOutputBuilder {
    /**
     * 검증된 blueprint 출력 DTO를 기반으로 최종 coordination 응답 DTO를 만든다.
     * TODO: 현재는 blueprint 값을 그대로 응답에 옮기는 임시 빌더다.
     * TODO: matchedItemsBySlot, weather를 활용한 실제 최종 응답 조립은 추후 구현한다.
     */
    @LogStage("coordination.build")
    public CoordinationOutputDto build(
        NormalizedBlueprintDto validatedBlueprint,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot
    ) {
        RawBlueprintDto.AiBlueprint aiBlueprint = validatedBlueprint.rawBlueprint().aiBlueprint();
        return new CoordinationOutputDto(
            null,
            aiBlueprint.tpoType(),
            aiBlueprint.styleType(),
            aiBlueprint.stylingRuleApplied(),
            buildCoordinationOutputs(validatedBlueprint, matchedItemsBySlot)
        );
    }

    private List<CoordinationItemOutputDto> buildCoordinationOutputs(
        NormalizedBlueprintDto validatedBlueprint,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot
    ) {
        return Arrays.stream(CategoryType.values())
            .map(categoryType -> toCoordinationOutput(
                validatedBlueprint.itemsBySlot().get(categoryType),
                matchedItemsBySlot == null ? null : matchedItemsBySlot.get(categoryType)
            ))
            .toList();
    }

    private CoordinationItemOutputDto toCoordinationOutput(
        RawBlueprintDto.ItemInfo item,
        ItemMatcher.MatchedItem matchedItem
    ) {
        if (item == null) {
            return new CoordinationItemOutputDto(
                null,
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

        RawBlueprintDto.Attributes attributes = item.attributes();
        ProductDto matchedProduct = matchedItem == null ? null : matchedItem.product();
        String itemName = matchedProduct != null && matchedProduct.getName() != null && !matchedProduct.getName().isBlank()
            ? matchedProduct.getName()
            : item.itemName();
        String imageUrl = matchedProduct == null ? null : matchedProduct.getImageUrl();
        double matchScore = matchedItem == null ? 0.0 : matchedItem.matchScore();
        return new CoordinationItemOutputDto(
            item.slotKey(),
            itemName,
            imageUrl,
            item.category(),
            matchScore,
            readTempRangeValue(item.tempRange(), 0),
            readTempRangeValue(item.tempRange(), 1),
            item.priority(),
            item.reasoning(),
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
