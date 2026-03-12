package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.internal.BlueprintValidationDto;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ItemMatcher {
    public Map<CategoryType, MatchedItem> matchAll(
        Map<CategoryType, List<ShoppingProductCandidate>> slotCandidates,
        BlueprintValidationDto.ValidatedBlueprint validatedBlueprint
    ) {
        Map<CategoryType, MatchedItem> matchedItemsBySlot = new EnumMap<>(CategoryType.class);
        Map<CategoryType, BlueprintValidationDto.Slot> slotsByCategory =
            validatedBlueprint == null || validatedBlueprint.slotsByCategory() == null
                ? Map.of()
                : validatedBlueprint.slotsByCategory();

        for (CategoryType categoryType : CategoryType.values()) {
            List<ShoppingProductCandidate> candidates = slotCandidates == null ? null : slotCandidates.get(categoryType);
            // TODO: 실제 매칭 로직이 들어오기 전까지는 첫 후보를 그대로 선택한다.
            ShoppingProductCandidate selected = selectBestCandidate(candidates);
            // TODO: 실제 점수 계산 규칙 도입 전 임시 점수.
            double matchScore = selected == null ? 0.0 : 1.0;
            BlueprintValidationDto.Slot slotData = slotsByCategory.get(categoryType);
            matchedItemsBySlot.put(categoryType, new MatchedItem(slotData, matchScore, selected));
        }
        return matchedItemsBySlot;
    }

    private ShoppingProductCandidate selectBestCandidate(List<ShoppingProductCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        // TODO: 현재는 실매칭이 아니라 첫 후보 반환용 stub이다.
        return candidates.get(0);
    }

    public record MatchedItem(
        BlueprintValidationDto.Slot slotData,
        double matchScore,
        ShoppingProductCandidate product
    ) {
    }
}
