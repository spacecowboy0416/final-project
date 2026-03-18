package com.finalproject.coordi.recommendation.service.coordination;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.productSearch.ProductSearchStage.ProductSearchStageResult;

import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 요청 태그와 후보 상품을 입력받아 슬롯별 매칭 결과를 만든다.
 * 현재는 Product Search 리팩토링 단계이므로 빈 결과를 반환한다.
 */
@Component
public class CoordinationStage {
    @LogStage("coordination.match")
    public CoordinationStageResult match(
        NormalizedBlueprintDto normalizedBlueprint,
        ProductSearchStageResult productSearchResult
    ) {
        return new CoordinationStageResult(Collections.emptyMap());
    }

    public record CoordinationStageResult(
        Map<String, Object> matchedItemsBySlot
    ) {
    }
}
