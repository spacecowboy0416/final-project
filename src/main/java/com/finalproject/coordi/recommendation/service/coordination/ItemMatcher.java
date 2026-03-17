package com.finalproject.coordi.recommendation.service.coordination;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ItemMatcher {
    public Map<CategoryType, MatchedItem> matchAll(
        Map<CategoryType, List<ProductDto>> persistedProductsBySlot,
        NormalizedBlueprintDto validatedBlueprint
    ) {
        Map<CategoryType, MatchedItem> matchedItemsBySlot = new EnumMap<>(CategoryType.class);
        Map<CategoryType, RawBlueprintDto.ItemInfo> itemsBySlot =
            validatedBlueprint == null || validatedBlueprint.itemsBySlot() == null
                ? Map.of()
                : validatedBlueprint.itemsBySlot();

        for (CategoryType categoryType : CategoryType.values()) {
            List<ProductDto> persistedProducts = persistedProductsBySlot == null ? null : persistedProductsBySlot.get(categoryType);
            // TODO: 실제 매칭 로직이 들어오기 전까지는 첫 persisted product를 그대로 선택한다.
            ProductDto selectedProduct = selectBestProduct(persistedProducts);
            // TODO: 실제 점수 계산 규칙 도입 전 임시 점수.
            double matchScore = selectedProduct == null ? 0.0 : 1.0;
            RawBlueprintDto.ItemInfo itemInfo = itemsBySlot.get(categoryType);
            matchedItemsBySlot.put(categoryType, new MatchedItem(itemInfo, matchScore, selectedProduct));
        }
        return matchedItemsBySlot;
    }

    private ProductDto selectBestProduct(List<ProductDto> persistedProducts) {
        if (persistedProducts == null || persistedProducts.isEmpty()) {
            return null;
        }
        // TODO: 현재는 실매칭이 아니라 첫 persisted product 반환용 stub이다.
        return persistedProducts.get(0);
    }

    public record MatchedItem(
        RawBlueprintDto.ItemInfo itemInfo,
        double matchScore,
        ProductDto product
    ) {
    }
}


