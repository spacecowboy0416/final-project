package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import com.finalproject.coordi.recommendation.dto.api.CoordinationResponseDto;
import com.finalproject.coordi.recommendation.dto.internal.AiBlueprintConstraintDto;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 3-2단계 스켈레톤: 슬롯 후보 상품 중 코디 적합 아이템을 DB 기준으로 병렬 매핑한다.
 */
@Component
public class ParallelBestItemMapping {
    public List<CoordinationResponseDto.ItemResult> mapBestItems(
        AiBlueprintConstraintDto.Envelope validatedCoordination,
        Map<SlotKey, List<ShoppingProductCandidate>> slotCandidates
    ) {
        throw new UnsupportedOperationException("TODO: 최적 아이템 DB 매핑 병렬 구현");
    }
}
