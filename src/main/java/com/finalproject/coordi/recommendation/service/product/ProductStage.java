package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.ShoppingSearchQuery;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Product 단계의 읽기 쉬운 진입점이다.
 *
 * [1] blueprint에서 slot별 검색어를 만든다.
 * [2] 검색어로 상품을 조회한다.
 * [3] 조회 결과를 DB와 이미지 저장소에 반영한다.
 */
@Component
@RequiredArgsConstructor
public class ProductStage {
    private final QueryExtractor queryExtractor;
    private final ShoppingSearcher shoppingSearcher;
    private final ProductDataUploader dataUploader;

    @LogStage("product.process")
    public void process(NormalizedBlueprintDto blueprint) {
        Map<CategoryType, ShoppingSearchQuery> queries = queryExtractor.extract(blueprint);
        Map<CategoryType, List<SearchedProduct>> productsBySlot = shoppingSearcher.searchAll(queries);

        dataUploader.uploadAll(blueprint, productsBySlot);
    }
}
