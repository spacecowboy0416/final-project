package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BlueprintResponseBuilder {
    public CoordinationOutputDto build(
        Object validatedBlueprint,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot
    ) {
        // TODO: 검증된 블루프린트와 매칭 결과를 최종 API 응답으로 조립한다.
        return null;
    }
}



