package com.finalproject.coordi.recommendation.service.imagefilter;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

// noop 구현체
@Component
public class ImageFilterStage {

    public ImageFilterStageResult filter(Map<CategoryType, List<SearchedProduct>> productsBySlot) {
        return new ImageFilterStageResult(productsBySlot);
    }

    public record ImageFilterStageResult(
        Map<CategoryType, List<SearchedProduct>> filteredProductsBySlot
    ) {
    }
}