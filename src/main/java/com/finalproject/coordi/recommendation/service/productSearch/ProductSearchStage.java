package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Product Search 단계에서 쿼리 생성과 슬롯별 검색을 수행한다.
 */
@Component
@RequiredArgsConstructor
public class ProductSearchStage {
    private final SearchQueryExtractor searchQueryExtractor;
    private final ShoppingSearcher shoppingSearcher;

    @LogStage("product.search")
    public ProductSearchStageResult search(
        NormalizedBlueprintDto normalizedBlueprint,
        Boolean brandEnabled
    ) {
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries = searchQueryExtractor.extract(
            normalizedBlueprint,
            brandEnabled
        );
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot = shoppingSearcher.searchBySlot(slotSearchQueries);
        return new ProductSearchStageResult(slotSearchQueries, searchedProductsBySlot);
    }

    public record ProductSearchStageResult(
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries,
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
    }
}
