package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import com.finalproject.coordi.recommendation.dto.internal.AiBlueprintConstraintDto;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 3-1단계 스켈레톤: 검증된 코디의 각 슬롯을 네이버 쇼핑으로 병렬 검색한다.
 */
@Component
public class ParallelShoppingSearch {
    public Map<SlotKey, List<ShoppingProductCandidate>> searchBySlots(AiBlueprintConstraintDto.Envelope validatedCoordination) {
        throw new UnsupportedOperationException("TODO: 슬롯별 쇼핑 병렬 검색 구현");
    }
}
