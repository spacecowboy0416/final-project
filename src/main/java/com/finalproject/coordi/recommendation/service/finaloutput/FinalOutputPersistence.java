package com.finalproject.coordi.recommendation.service.finaloutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.domain.enums.CodedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import com.finalproject.coordi.recommendation.service.productSearch.SearchedProductsBySlot;
import com.finalproject.coordi.recommendation.service.productSearch.SlotSearchQueries;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * final output 단계에서 추천 결과를 recommendation/recommendation_item 테이블에 저장한다.
 */
@Service
@RequiredArgsConstructor
public class FinalOutputPersistence {
    private static final String INPUT_MODE_TEXT = "TEXT";
    private static final String PRODUCT_OPTION_SEARCH_TOP1 = "SEARCH_TOP1";
    private static final String SOURCE_TYPE_PRODUCT = "PRODUCT";
    private static final String SOURCE_TYPE_MAIN_ITEM = "MAIN_ITEM";
    private static final String DEFAULT_PRODUCT_SOURCE = "NAVER";
    private static final String MAIN_ITEM_PRODUCT_SOURCE = "MAIN_UPLOAD";
    private static final String DEFAULT_MAIN_ITEM_NAME = "main_item";
    private static final double DEFAULT_MATCH_SCORE = 1.0d;
    private static final String EMPTY_JSON = "{}";
    private static final int TEMP_RANGE_MIN_INDEX = 0;
    private static final int TEMP_RANGE_MAX_INDEX = 1;
    private static final int TEMP_RANGE_REQUIRED_SIZE = 2;

    private final RecommendationMapper recommendationMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(
        Long userId,
        UserRequestDto request,
        NormalizedBlueprintDto normalizedBlueprint,
        SearchedProductsBySlot effectiveProducts,
        SlotSearchQueries slotSearchQueries
    ) {
        if (userId == null) {
            return;
        }

        var aiBlueprint = normalizedBlueprint.aiBlueprint();
        RecommendationDto recommendationDto = RecommendationDto.builder()
            .userId(userId)
            .inputMode(INPUT_MODE_TEXT)
            .inputText(request.naturalText())
            .productOption(PRODUCT_OPTION_SEARCH_TOP1)
            .tpoType(aiBlueprint.tpoType().getCode())
            .styleType(aiBlueprint.styleType().getCode())
            .isSaved(true)
            .aiBlueprint(toJson(normalizedBlueprint.rawBlueprint()))
            .aiExplanation(extractAiExplanation(normalizedBlueprint))
            .build();

        recommendationMapper.insertRecommendation(recommendationDto);
        Long recId = recommendationDto.getRecId();
        if (recId == null) {
            return;
        }

        CategoryType anchorSlot = normalizedBlueprint.anchorSlot();
        Set<CategoryType> persistableSlots = resolvePersistableSlots(normalizedBlueprint, effectiveProducts);

        for (CategoryType categoryType : persistableSlots) {
            boolean isMainItem = categoryType == anchorSlot;
            Long productId = upsertProduct(categoryType, normalizedBlueprint, effectiveProducts, isMainItem);
            if (productId == null) {
                continue;
            }

            RecommendationItemDto recommendationItemDto = RecommendationItemDto.builder()
                .recId(recId)
                .slotKey(categoryType.getCode())
                .sourceType(isMainItem ? SOURCE_TYPE_MAIN_ITEM : SOURCE_TYPE_PRODUCT)
                .productId(productId)
                .searchQuery(extractSearchQuery(categoryType, slotSearchQueries))
                .priority(extractPriority(categoryType, normalizedBlueprint))
                .matchScore(DEFAULT_MATCH_SCORE)
                .reason(extractReason(categoryType, normalizedBlueprint))
                .build();
            recommendationMapper.insertRecommendationItem(recommendationItemDto);
        }
    }

    // 최종 응답 기준(옵션 슬롯 숨김 규칙 포함)으로 실제 저장 대상 슬롯만 선별한다.
    private Set<CategoryType> resolvePersistableSlots(
        NormalizedBlueprintDto normalizedBlueprint,
        SearchedProductsBySlot effectiveProducts
    ) {
        LinkedHashSet<CategoryType> persistableSlots = new LinkedHashSet<>();
        CategoryType anchorSlot = normalizedBlueprint.anchorSlot();
        for (CategoryType categoryType : CategoryType.values()) {
            CoordinationItemOutputDto outputItem = toCoordinationOutputItem(normalizedBlueprint, effectiveProducts, categoryType);
            if (FinalOutputBuilder.shouldSkipOptionalSlot(outputItem)) {
                continue;
            }
            if (
                categoryType != anchorSlot
                    && (outputItem.marketplaceProductId() == null || outputItem.marketplaceProductId().isBlank())
            ) {
                continue;
            }
            persistableSlots.add(categoryType);
        }
        return persistableSlots;
    }

    // FinalOutputBuilder의 출력 규칙을 저장 단계에서도 동일하게 재사용하기 위한 축약 DTO 생성기.
    private CoordinationItemOutputDto toCoordinationOutputItem(
        NormalizedBlueprintDto normalizedBlueprint,
        SearchedProductsBySlot effectiveProducts,
        CategoryType categoryType
    ) {
        RawBlueprintDto.ItemInfo itemInfo = requiredItem(normalizedBlueprint, categoryType);
        CategoryType anchorSlot = normalizedBlueprint.anchorSlot();
        boolean isMainItem = categoryType == anchorSlot;
        SearchedProduct top1Product = isMainItem
            ? null
            : EffectiveProductSelector.extractTop1Product(categoryType, effectiveProducts);
        Integer tempMin = itemInfo.tempRange().get(0);
        Integer tempMax = itemInfo.tempRange().get(1);

        return new CoordinationItemOutputDto(
            categoryType,
            top1Product == null ? itemInfo.itemName() : top1Product.productName(),
            top1Product == null ? null : top1Product.productImageUrl(),
            isMainItem,
            top1Product == null ? null : top1Product.marketplaceProvider(),
            top1Product == null ? null : top1Product.marketplaceProductId(),
            top1Product == null || isMainItem ? null : top1Product.brandName(),
            top1Product == null ? null : top1Product.salePrice(),
            top1Product == null ? null : top1Product.productDetailUrl(),
            itemInfo.category(),
            top1Product == null ? 0.0d : DEFAULT_MATCH_SCORE,
            tempMin,
            tempMax,
            itemInfo.priority(),
            itemInfo.reasoning(),
            itemInfo.attributes().color(),
            itemInfo.attributes().material(),
            itemInfo.attributes().fit(),
            itemInfo.attributes().style()
        );
    }

    private Long upsertProduct(
        CategoryType categoryType,
        NormalizedBlueprintDto normalizedBlueprint,
        SearchedProductsBySlot effectiveProducts,
        boolean isMainItem
    ) {
        ProductCategoryCode productCategoryCode = ProductCategoryCode.fromSlotType(categoryType);
        Long categoryId = recommendationMapper.findCategoryIdByCode(productCategoryCode);
        if (categoryId == null) {
            return null;
        }

        SearchedProduct top1Product = isMainItem
            ? null
            : EffectiveProductSelector.extractTop1Product(categoryType, effectiveProducts);
        if (!isMainItem && !hasMarketplaceId(top1Product)) {
            return null;
        }

        RawBlueprintDto.ItemInfo itemInfo = requiredItem(normalizedBlueprint, categoryType);
        ProductDto productDto = buildProductDto(categoryId, itemInfo, top1Product);
        recommendationMapper.upsertProduct(productDto);
        return productDto.getProductId();
    }

    // ProductDto는 순수 영속 DTO로 유지하고 조립 규칙은 서비스 계층에서 처리한다.
    private ProductDto buildProductDto(
        Long categoryId,
        RawBlueprintDto.ItemInfo itemInfo,
        SearchedProduct top1Product
    ) {
        boolean isMainItem = top1Product == null;
        String source = isMainItem ? MAIN_ITEM_PRODUCT_SOURCE : resolveSource(top1Product.marketplaceProvider());
        String externalId = isMainItem ? UUID.randomUUID().toString() : top1Product.marketplaceProductId();

        return ProductDto.builder()
            .source(source)
            .externalId(externalId)
            .categoryId(categoryId)
            .gender(toCode(itemInfo.attributes().gender()))
            .name(isMainItem ? resolveMainItemName(itemInfo.itemName()) : top1Product.productName())
            .brand(isMainItem ? null : top1Product.brandName())
            .price(isMainItem ? null : top1Product.salePrice())
            .imageUrl(isMainItem ? null : top1Product.productImageUrl())
            .link(isMainItem ? null : top1Product.productDetailUrl())
            .color(toCode(itemInfo.attributes().color()))
            .material(toCode(itemInfo.attributes().material()))
            .fit(toCode(itemInfo.attributes().fit()))
            .style(toCode(itemInfo.attributes().style()))
            .season(null)
            .tempMin(resolveTempMin(itemInfo.tempRange()))
            .tempMax(resolveTempMax(itemInfo.tempRange()))
            .build();
    }

    private String resolveSource(String marketplaceProvider) {
        if (marketplaceProvider == null || marketplaceProvider.isBlank()) {
            return DEFAULT_PRODUCT_SOURCE;
        }
        return marketplaceProvider;
    }

    private String resolveMainItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return DEFAULT_MAIN_ITEM_NAME;
        }
        return itemName;
    }

    private String toCode(CodedEnum codedEnum) {
        return codedEnum == null ? null : codedEnum.getCode();
    }

    private Integer resolveTempMin(List<Integer> tempRange) {
        if (!isValidTempRange(tempRange)) {
            return null;
        }
        return tempRange.get(TEMP_RANGE_MIN_INDEX);
    }

    private Integer resolveTempMax(List<Integer> tempRange) {
        if (!isValidTempRange(tempRange)) {
            return null;
        }
        return tempRange.get(TEMP_RANGE_MAX_INDEX);
    }

    private boolean isValidTempRange(List<Integer> tempRange) {
        return tempRange != null && tempRange.size() == TEMP_RANGE_REQUIRED_SIZE;
    }

    // slotSearchQueries 에서 해당 슬롯의 검색 키워드를 추출한다.
    private String extractSearchQuery(
        CategoryType categoryType,
        SlotSearchQueries slotSearchQueries
    ) {
        return slotSearchQueries.searchKeywordOf(categoryType);
    }

    private String extractPriority(CategoryType categoryType, NormalizedBlueprintDto normalizedBlueprint) {
        return requiredItem(normalizedBlueprint, categoryType).priority().getCode();
    }

    private String extractReason(CategoryType categoryType, NormalizedBlueprintDto normalizedBlueprint) {
        return requiredItem(normalizedBlueprint, categoryType).reasoning();
    }

    private String extractAiExplanation(NormalizedBlueprintDto normalizedBlueprint) {
        String aiExplanation = normalizedBlueprint.aiBlueprint().aiExplanation();
        return aiExplanation == null ? "" : aiExplanation;
    }

    private String toJson(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException exception) {
            return EMPTY_JSON;
        }
    }

    private boolean hasMarketplaceId(SearchedProduct top1Product) {
        return top1Product != null
            && top1Product.marketplaceProductId() != null
            && !top1Product.marketplaceProductId().isBlank();
    }

    private RawBlueprintDto.ItemInfo requiredItem(NormalizedBlueprintDto normalizedBlueprint, CategoryType categoryType) {
        RawBlueprintDto.ItemInfo itemInfo = normalizedBlueprint.itemBySlot(categoryType);
        if (itemInfo == null) {
            throw new IllegalStateException("정규화된 blueprint에 슬롯 아이템이 없습니다. slot=" + categoryType);
        }
        return itemInfo;
    }
}

