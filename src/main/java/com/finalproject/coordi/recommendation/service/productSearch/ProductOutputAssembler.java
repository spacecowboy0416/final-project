package com.finalproject.coordi.recommendation.service.productSearch;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * ProductSearch 단계 마지막에서 사용자 노출용 coordination 출력을 조립한다.
 */
@Component
public class ProductOutputAssembler {
    private static final List<CategoryType> OPTIONAL_OUTPUT_SLOTS = List.of(
        CategoryType.HEADWEAR,
        CategoryType.ACCESSORIES
    );

    public CoordinationOutputDto assemble(
        NormalizedBlueprintDto normalizedBlueprint,
        SearchedProductsBySlot searchedProductsBySlot,
        SlotSearchQueries slotSearchQueries
    ) {
        RawBlueprintDto.AiBlueprint aiBlueprint = normalizedBlueprint.aiBlueprint();
        List<CoordinationItemOutputDto> coordinationItems = buildCoordinationItems(normalizedBlueprint, searchedProductsBySlot);
        return new CoordinationOutputDto(
            UUID.randomUUID().toString(),
            aiBlueprint.tpoType(),
            aiBlueprint.styleType(),
            aiBlueprint.aiExplanation(),
            coordinationItems,
            buildQueryMap(slotSearchQueries)
        );
    }

    private List<CoordinationItemOutputDto> buildCoordinationItems(
        NormalizedBlueprintDto normalizedBlueprint,
        SearchedProductsBySlot searchedProductsBySlot
    ) {
        List<CoordinationItemOutputDto> coordinationItems = new ArrayList<>();
        CategoryType anchorSlot = normalizedBlueprint.anchorSlot();
        for (CategoryType categoryType : CategoryType.values()) {
            RawBlueprintDto.ItemInfo itemInfo = normalizedBlueprint.itemBySlot(categoryType);
            boolean isAnchorSlot = categoryType == anchorSlot;
            SearchedProduct top1Product = isAnchorSlot ? null : searchedProductsBySlot.top1(categoryType);

            Integer tempMin = null;
            Integer tempMax = null;
            if (itemInfo != null && itemInfo.tempRange() != null && itemInfo.tempRange().size() == 2) {
                tempMin = itemInfo.tempRange().get(0);
                tempMax = itemInfo.tempRange().get(1);
            }

            CoordinationItemOutputDto coordinationItem = new CoordinationItemOutputDto(
                categoryType,
                top1Product == null ? (itemInfo == null ? "" : itemInfo.itemName()) : top1Product.productName(),
                top1Product == null ? null : top1Product.productImageUrl(),
                isAnchorSlot,
                top1Product == null ? null : top1Product.marketplaceProvider(),
                top1Product == null ? null : top1Product.marketplaceProductId(),
                top1Product == null || isAnchorSlot ? null : top1Product.brandName(),
                top1Product == null ? null : top1Product.salePrice(),
                top1Product == null ? null : top1Product.productDetailUrl(),
                itemInfo == null ? null : itemInfo.category(),
                top1Product == null ? 0.0d : 1.0d,
                tempMin,
                tempMax,
                itemInfo == null ? null : itemInfo.priority(),
                itemInfo == null ? "" : itemInfo.reasoning(),
                itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().color(),
                itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().material(),
                itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().fit(),
                itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().style()
            );

            // 선택 슬롯은 출력 근거가 없으면 사용자 화면과 저장 모두에서 제외한다.
            if (shouldSkipOptionalSlot(coordinationItem)) {
                continue;
            }

            coordinationItems.add(coordinationItem);
        }
        return coordinationItems;
    }

    private Map<String, String> buildQueryMap(SlotSearchQueries slotSearchQueries) {
        if (slotSearchQueries == null || slotSearchQueries.isEmpty()) {
            return Map.of();
        }
        Map<String, String> queryMap = new LinkedHashMap<>();
        slotSearchQueries.forEach((slotKey, query) -> {
            if (slotKey == null || query == null || query.searchKeyword() == null) {
                return;
            }
            queryMap.put(slotKey.getCode(), query.searchKeyword());
        });
        return queryMap;
    }

    private boolean shouldSkipOptionalSlot(CoordinationItemOutputDto coordinationItem) {
        if (coordinationItem == null || !OPTIONAL_OUTPUT_SLOTS.contains(coordinationItem.slotKey())) {
            return false;
        }
        return !coordinationItem.isMyItem()
            && !hasText(coordinationItem.imageUrl())
            && !hasText(coordinationItem.productDetailUrl())
            && coordinationItem.salePrice() == null
            && !hasText(coordinationItem.itemName());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
