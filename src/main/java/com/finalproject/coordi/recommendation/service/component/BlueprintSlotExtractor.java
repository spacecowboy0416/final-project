package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.internal.AiBlueprintConstraintDto;
import com.finalproject.coordi.recommendation.dto.internal.BlueprintSlot;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 스켈레톤: 검증된 AI 코디에서 슬롯 단위 blueprint를 추출한다.
 */
@Component
public class BlueprintSlotExtractor {
    public List<BlueprintSlot> extract(AiBlueprintConstraintDto.Envelope validatedCoordination) {
        throw new UnsupportedOperationException("TODO: 슬롯 추출 구현");
    }
}
