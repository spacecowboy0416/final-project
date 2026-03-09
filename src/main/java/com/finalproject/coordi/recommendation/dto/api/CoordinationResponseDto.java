package com.finalproject.coordi.recommendation.dto.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.BlueprintSource;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.RecommendationStatus;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SlotKey;
import com.finalproject.coordi.recommendation.dao.ProductDao;
import com.finalproject.coordi.recommendation.domain.enums.CoordiContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.CoordiContextEnums.TpoType;
import com.finalproject.coordi.recommendation.dto.internal.BlueprintSlot;
import java.util.List;

/**
 * 코디 추천 실행 응답 계약.
 */
public record CoordinationResponseDto(
    String coordinationId,
    RecommendationStatus status,
    BlueprintSource blueprintSource,
    TpoType tpoType,
    StyleMode styleMode,
    JsonNode aiBlueprint,
    List<CoordinationOutputDto> coordination,
    List<ItemResult> finalItems
) {
    public record ItemResult(
        SlotKey slotKey,
        String itemName,
        String searchQuery,
        String reasoning,
        PriorityType priority,
        double matchScore,
        ProductPreview product
    ) {
        public static ItemResult from(
            BlueprintSlot slotBlueprint,
            double matchScore,
            ProductDao persistedProduct
        ) {
            return new ItemResult(
                slotBlueprint.slotKey(),
                slotBlueprint.itemName(),
                slotBlueprint.searchQuery(),
                slotBlueprint.reasoning(),
                slotBlueprint.priority(),
                matchScore,
                ProductPreview.from(persistedProduct)
            );
        }
    }

    public record ProductPreview(
        String name,
        int price,
        String imageUrl,
        String link
    ) {
        public static ProductPreview from(ProductDao persistedProduct) {
            if (persistedProduct == null) {
                return null;
            }
            return new ProductPreview(
                persistedProduct.getName(),
                persistedProduct.getPrice(),
                persistedProduct.getImageUrl(),
                persistedProduct.getLink()
            );
        }
    }
}
