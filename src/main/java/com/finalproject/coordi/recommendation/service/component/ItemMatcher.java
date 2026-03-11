package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.stereotype.Component;

@Component
public class ItemMatcher {
    public Map<CategoryType, MatchedItem> matchAll(
        Map<CategoryType, List<ShoppingProductCandidate>> slotCandidates,
        Object validatedBlueprint
    ) {
        // TODO: 슬롯별 후보군에서 최적 상품을 선택해 매칭 결과를 만든다.
        return new HashMap<>();
    }

    public record MatchedItem(
        Object slotData,
        double matchScore,
        ShoppingProductCandidate product
    ) {
    }
}

