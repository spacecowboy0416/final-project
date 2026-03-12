package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingSearchQuery;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ItemSearchQueryExtractor {
    public Map<CategoryType, ShoppingSearchQuery> extract(Object validatedBlueprint) {
        // TODO: 검증된 블루프린트에서 슬롯별 검색 쿼리를 추출한다.
        return null;
    }
}

