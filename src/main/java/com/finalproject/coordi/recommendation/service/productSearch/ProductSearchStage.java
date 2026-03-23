package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
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
    private final ProductOutputAssembler productOutputAssembler;

    @LogStage("product.search")
    public ProductSearchStageResult search(
        NormalizedBlueprintDto normalizedBlueprint,
        Boolean brandEnabled
    ) {
        SlotSearchQueries slotSearchQueries = searchQueryExtractor.extract(
            normalizedBlueprint,
            brandEnabled
        );
        SearchedProductsBySlot searchedProductsBySlot = shoppingSearcher.searchBySlot(slotSearchQueries);
        CoordinationOutputDto coordinationOutput = productOutputAssembler.assemble(
            normalizedBlueprint,
            searchedProductsBySlot,
            slotSearchQueries
        );
        return new ProductSearchStageResult(slotSearchQueries, searchedProductsBySlot, coordinationOutput);
    }

    public record ProductSearchStageResult(
        SlotSearchQueries slotSearchQueries,
        SearchedProductsBySlot searchedProductsBySlot,
        CoordinationOutputDto coordinationOutput
    ) {
    }
}
