package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ItemUpserter {
    public void upsertAll(Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot) {
        // TODO: 매칭 결과를 추천 아이템 테이블에 업서트한다.
    }
}

