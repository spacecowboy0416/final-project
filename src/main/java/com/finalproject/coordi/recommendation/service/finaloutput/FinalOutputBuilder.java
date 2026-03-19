package com.finalproject.coordi.recommendation.service.finaloutput;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 최종 API 응답 DTO를 조립한다.
 * effectiveProducts(필터 적용 여부와 무관한 최종 상품 맵)에서 슬롯별 TOP1을 선택해
 * 코디 아이템을 만든다. 저장은 표준 추천 경로에서만 별도로 수행한다.
 */
@Component
@RequiredArgsConstructor
public class FinalOutputBuilder {
    private static final String EMPTY_STYLING_RULE = "";

    private final FinalOutputPersistenceService finalOutputPersistenceService;

    /**
     * @param effectiveProducts 실제 최종 출력에 사용할 슬롯별 상품 맵
     *                          (FAST: 비필터 searchedProductsBySlot,
     *                           LEGACY: imageFilter 통과 filteredProductsBySlot)
     */
    public CoordinationOutputDto build(
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        RawBlueprintDto.AiBlueprint aiBlueprint = normalizedBlueprint == null ? null : normalizedBlueprint.aiBlueprint();
        String blueprintId = UUID.randomUUID().toString();
        List<CoordinationItemOutputDto> coordinationItems = buildCoordinationItems(normalizedBlueprint, effectiveProducts);
        return new CoordinationOutputDto(
            blueprintId,
            aiBlueprint == null ? null : aiBlueprint.tpoType(),
            aiBlueprint == null ? null : aiBlueprint.styleType(),
            aiBlueprint == null ? EMPTY_STYLING_RULE : aiBlueprint.stylingRuleApplied(),
            coordinationItems
        );
    }

    public CoordinationOutputDto buildAndPersist(
        UserRequestDto request,
        Long userId,
        PayloadDto payload,
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        CoordinationOutputDto outputDto = build(normalizedBlueprint, effectiveProducts);
        finalOutputPersistenceService.save(userId, request, payload, normalizedBlueprint, effectiveProducts, slotSearchQueries);
        return outputDto;
    }

    private List<CoordinationItemOutputDto> buildCoordinationItems(
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        List<CoordinationItemOutputDto> coordinationItems = new ArrayList<>();
        for (CategoryType categoryType : CategoryType.values()) {
            var itemInfo = normalizedBlueprint == null ? null : normalizedBlueprint.itemBySlot(categoryType);
            SearchedProduct top1Product = extractTop1Product(categoryType, effectiveProducts);

            Integer tempMin = null;
            Integer tempMax = null;
            if (itemInfo != null && itemInfo.tempRange() != null && itemInfo.tempRange().size() == 2) {
                tempMin = itemInfo.tempRange().get(0);
                tempMax = itemInfo.tempRange().get(1);
            }

            coordinationItems.add(
                new CoordinationItemOutputDto(
                    categoryType,
                    top1Product == null ? (itemInfo == null ? "" : itemInfo.itemName()) : top1Product.productName(),
                    top1Product == null ? null : top1Product.productImageUrl(),
                    top1Product == null ? null : top1Product.brandName(),
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
                )
            );
        }
        return coordinationItems;
    }

    // 슬롯별 effectiveProducts 에서 TOP1 상품을 추출한다.
    private SearchedProduct extractTop1Product(
        CategoryType categoryType,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        if (effectiveProducts == null) {
            return null;
        }
        List<SearchedProduct> products = effectiveProducts.get(categoryType);
        if (products == null || products.isEmpty()) {
            return null;
        }
        return products.getFirst();
    }
}
