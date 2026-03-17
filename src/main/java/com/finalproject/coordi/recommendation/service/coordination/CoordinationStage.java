package com.finalproject.coordi.recommendation.service.coordination;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * coordination 단계의 DB 재조회와 item match를 묶는다.
 */
@Component
@RequiredArgsConstructor
public class CoordinationStage {
    private final PersistedProductReader persistedProductReader;
    private final ItemMatcher itemMatcher;

    @LogStage("coordination.coordinate")
    public CoordinationResult coordinate(
        NormalizedBlueprintDto validatedBlueprint,
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
        Map<CategoryType, List<ProductDto>> persistedProductsBySlot =
            persistedProductReader.readBySlot(searchedProductsBySlot);
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot =
            itemMatcher.matchAll(persistedProductsBySlot, validatedBlueprint);
        return new CoordinationResult(persistedProductsBySlot, matchedItemsBySlot);
    }

    public record CoordinationResult(
        Map<CategoryType, List<ProductDto>> persistedProductsBySlot,
        Map<CategoryType, ItemMatcher.MatchedItem> matchedItemsBySlot
    ) {
    }
}


