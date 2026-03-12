package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingSearchQuery;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ShoppingSearcher {
    public Map<CategoryType, List<ShoppingProductCandidate>> searchAll(
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        // TODO: 슬롯별 검색 쿼리를 쇼핑 API로 조회해 후보군을 반환한다.
        return null;
    }
}

