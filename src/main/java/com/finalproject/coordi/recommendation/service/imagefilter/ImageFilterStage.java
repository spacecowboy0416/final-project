package com.finalproject.coordi.recommendation.service.imagefilter;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ProductSearch 결과를 받아 이미지 1차 필터를 적용한다.
 */
@Component
@RequiredArgsConstructor
public class ImageFilterStage {
    private final FilteredProductSelector filteredProductSelector;

    @LogStage("image.filter")
    public FilteredProduct filter(Map<CategoryType, List<SearchedProduct>> searchedProduct) {
        Map<CategoryType, List<SearchedProduct>> filteredProductsBySlot =
            filteredProductSelector.select(searchedProduct);
        return new FilteredProduct(filteredProductsBySlot);
    }

    public record FilteredProduct(
        Map<CategoryType, List<SearchedProduct>> filteredProductsBySlot
    ) {
    }
}
