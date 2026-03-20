package com.finalproject.coordi.recommendation.service.finaloutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationDebugResponseDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
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
public class FinalOutputPersistenceService {
    private static final String INPUT_MODE_TEXT = "TEXT";
    private static final String PRODUCT_OPTION_SEARCH_TOP1 = "SEARCH_TOP1";
    private static final String SOURCE_TYPE_PRODUCT = "PRODUCT";
    private static final String SOURCE_TYPE_MAIN_ITEM = "MAIN_ITEM";
    private static final String DEFAULT_PRODUCT_SOURCE = "NAVER";
    private static final String MAIN_ITEM_PRODUCT_SOURCE = "MAIN_UPLOAD";
    private static final String DEFAULT_MAIN_ITEM_NAME = "main_item";
    private static final double DEFAULT_MATCH_SCORE = 1.0d;
    private static final String EMPTY_JSON = "{}";

    private final RecommendationMapper recommendationMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(
        Long userId,
        UserRequestDto request,
        PayloadDto payload,
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        if (userId == null || request == null || normalizedBlueprint == null) {
            return;
        }

        RecommendationDto recommendationDto = RecommendationDto.builder()
            .userId(userId)
            .inputMode(INPUT_MODE_TEXT)
            .inputText(request.naturalText())
            .productOption(PRODUCT_OPTION_SEARCH_TOP1)
            .tpoType(normalizedBlueprint.aiBlueprint() == null || normalizedBlueprint.aiBlueprint().tpoType() == null
                ? null
                : normalizedBlueprint.aiBlueprint().tpoType().getCode())
            .styleType(normalizedBlueprint.aiBlueprint() == null || normalizedBlueprint.aiBlueprint().styleType() == null
                ? null
                : normalizedBlueprint.aiBlueprint().styleType().getCode())
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
            boolean isAnchorSlot = categoryType == anchorSlot;
            Long productId = isAnchorSlot
                ? upsertMainItemProduct(categoryType, normalizedBlueprint)
                : upsertTop1Product(categoryType, normalizedBlueprint, effectiveProducts);
            if (productId == null) {
                continue;
            }

            RecommendationItemDto recommendationItemDto = RecommendationItemDto.builder()
                .recId(recId)
                .slotKey(categoryType.getCode())
                .sourceType(isAnchorSlot ? SOURCE_TYPE_MAIN_ITEM : SOURCE_TYPE_PRODUCT)
                .productId(productId)
                .searchQuery(extractSearchQuery(categoryType, slotSearchQueries))
                .priority(extractPriority(categoryType, normalizedBlueprint))
                .matchScore(DEFAULT_MATCH_SCORE)
                .reason(extractReason(categoryType, normalizedBlueprint))
                .build();
            recommendationMapper.insertRecommendationItem(recommendationItemDto);
        }
    }

    @Transactional
    public void saveDebugResult(
        Long userId,
        UserRequestDto request,
        RecommendationDebugResponseDto debugResult
    ) {
        if (userId == null || request == null || debugResult == null) {
            return;
        }

        RecommendationDto recommendationDto = RecommendationDto.builder()
            .userId(userId)
            .inputMode(INPUT_MODE_TEXT)
            .inputText(request.naturalText())
            .productOption(PRODUCT_OPTION_SEARCH_TOP1)
            .tpoType(debugResult.tpoType() == null ? null : debugResult.tpoType().getCode())
            .styleType(debugResult.styleType() == null ? null : debugResult.styleType().getCode())
            .isSaved(true)
            .aiBlueprint(toJson(debugResult.rawBlueprint()))
            .aiExplanation(debugResult.aiExplanation() == null ? "" : debugResult.aiExplanation())
            .build();

        recommendationMapper.insertRecommendation(recommendationDto);
        Long recId = recommendationDto.getRecId();
        if (recId == null) {
            return;
        }

        Set<CategoryType> persistableSlots = resolvePersistableSlots(debugResult);

        for (CategoryType categoryType : persistableSlots) {
            CoordinationItemOutputDto item = findCoordinationItem(debugResult, categoryType);
            if (item == null) {
                continue;
            }

            boolean isAnchorSlot = item.isMyItem();
            Long productId = isAnchorSlot
                ? upsertMainItemProduct(categoryType, item)
                : upsertProductFromCoordinationItem(categoryType, item);
            if (productId == null) {
                continue;
            }

            RecommendationItemDto recommendationItemDto = RecommendationItemDto.builder()
                .recId(recId)
                .slotKey(categoryType.getCode())
                .sourceType(isAnchorSlot ? SOURCE_TYPE_MAIN_ITEM : SOURCE_TYPE_PRODUCT)
                .productId(productId)
                .searchQuery(extractSearchQuery(categoryType, debugResult))
                .priority(item == null || item.priority() == null ? null : item.priority().getCode())
                .matchScore(item.matchScore() <= 0.0d ? DEFAULT_MATCH_SCORE : item.matchScore())
                .reason(item == null || item.reasoning() == null ? "" : item.reasoning())
                .build();
            recommendationMapper.insertRecommendationItem(recommendationItemDto);
        }
    }

    // 최종 응답 기준(옵션 슬롯 숨김 규칙 포함)으로 실제 저장 대상 슬롯만 선별한다.
    private Set<CategoryType> resolvePersistableSlots(
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        LinkedHashSet<CategoryType> persistableSlots = new LinkedHashSet<>();
        if (normalizedBlueprint == null) {
            return persistableSlots;
        }

        CategoryType anchorSlot = normalizedBlueprint.anchorSlot();
        for (CategoryType categoryType : CategoryType.values()) {
            CoordinationItemOutputDto outputItem = toCoordinationOutputItem(normalizedBlueprint, effectiveProducts, categoryType);
            if (outputItem == null || FinalOutputBuilder.shouldSkipOptionalSlot(outputItem)) {
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

    // 디버그 저장도 동일한 출력 기준으로 저장 대상 슬롯을 선별한다.
    private Set<CategoryType> resolvePersistableSlots(RecommendationDebugResponseDto debugResult) {
        LinkedHashSet<CategoryType> persistableSlots = new LinkedHashSet<>();
        if (debugResult == null || debugResult.coordination() == null) {
            return persistableSlots;
        }

        for (CoordinationItemOutputDto item : debugResult.coordination()) {
            if (item == null || item.slotKey() == null || FinalOutputBuilder.shouldSkipOptionalSlot(item)) {
                continue;
            }
            if (!item.isMyItem() && (item.marketplaceProductId() == null || item.marketplaceProductId().isBlank())) {
                continue;
            }
            persistableSlots.add(item.slotKey());
        }
        return persistableSlots;
    }

    // FinalOutputBuilder의 출력 규칙을 저장 단계에서도 동일하게 재사용하기 위한 축약 DTO 생성기.
    private CoordinationItemOutputDto toCoordinationOutputItem(
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts,
        CategoryType categoryType
    ) {
        if (normalizedBlueprint == null || categoryType == null) {
            return null;
        }

        var itemInfo = normalizedBlueprint.itemBySlot(categoryType);
        CategoryType anchorSlot = normalizedBlueprint.anchorSlot();
        boolean isAnchorSlot = categoryType == anchorSlot;
        SearchedProduct top1Product = isAnchorSlot
            ? null
            : EffectiveProductSelector.extractTop1Product(categoryType, effectiveProducts);

        Integer tempMin = null;
        Integer tempMax = null;
        if (itemInfo != null && itemInfo.tempRange() != null && itemInfo.tempRange().size() == 2) {
            tempMin = itemInfo.tempRange().get(0);
            tempMax = itemInfo.tempRange().get(1);
        }

        return new CoordinationItemOutputDto(
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
            top1Product == null ? 0.0d : DEFAULT_MATCH_SCORE,
            tempMin,
            tempMax,
            itemInfo == null ? null : itemInfo.priority(),
            itemInfo == null ? "" : itemInfo.reasoning(),
            itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().color(),
            itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().material(),
            itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().fit(),
            itemInfo == null || itemInfo.attributes() == null ? null : itemInfo.attributes().style()
        );
    }

    private Long upsertMainItemProduct(
        CategoryType anchorSlot,
        NormalizedBlueprintDto normalizedBlueprint
    ) {
        if (anchorSlot == null || normalizedBlueprint == null) {
            return null;
        }

        ProductCategoryCode productCategoryCode = ProductCategoryCode.fromSlotType(anchorSlot);
        Long categoryId = recommendationMapper.findCategoryIdByCode(productCategoryCode.getCode());
        if (categoryId == null) {
            return null;
        }

        var itemInfo = normalizedBlueprint.itemBySlot(anchorSlot);
        Integer tempMin = null;
        Integer tempMax = null;
        if (itemInfo != null && itemInfo.tempRange() != null && itemInfo.tempRange().size() == 2) {
            tempMin = itemInfo.tempRange().get(0);
            tempMax = itemInfo.tempRange().get(1);
        }

        String externalId = UUID.randomUUID().toString();
        ProductDto productDto = ProductDto.builder()
            .source(MAIN_ITEM_PRODUCT_SOURCE)
            .externalId(externalId)
            .categoryId(categoryId)
            .gender(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().gender() == null
                ? null
                : itemInfo.attributes().gender().getCode())
            .name(resolveMainItemName(itemInfo == null ? null : itemInfo.itemName()))
            .brand(null)
            .price(null)
            .imageUrl(null)
            .link(null)
            .color(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().color() == null
                ? null
                : itemInfo.attributes().color().getCode())
            .material(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().material() == null
                ? null
                : itemInfo.attributes().material().getCode())
            .fit(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().fit() == null
                ? null
                : itemInfo.attributes().fit().getCode())
            .style(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().style() == null
                ? null
                : itemInfo.attributes().style().getCode())
            .season(null)
            .tempMin(tempMin)
            .tempMax(tempMax)
            .build();

        recommendationMapper.upsertProduct(productDto);
        return recommendationMapper.findProductIdBySourceAndExternalId(MAIN_ITEM_PRODUCT_SOURCE, externalId);
    }

    private Long upsertMainItemProduct(
        CategoryType categoryType,
        CoordinationItemOutputDto item
    ) {
        if (categoryType == null || item == null) {
            return null;
        }

        ProductCategoryCode productCategoryCode = ProductCategoryCode.fromSlotType(categoryType);
        Long categoryId = recommendationMapper.findCategoryIdByCode(productCategoryCode.getCode());
        if (categoryId == null) {
            return null;
        }

        String externalId = UUID.randomUUID().toString();
        ProductDto productDto = ProductDto.builder()
            .source(MAIN_ITEM_PRODUCT_SOURCE)
            .externalId(externalId)
            .categoryId(categoryId)
            .gender(null)
            .name(resolveMainItemName(item.itemName()))
            .brand(null)
            .price(null)
            .imageUrl(null)
            .link(null)
            .color(item.color() == null ? null : item.color().getCode())
            .material(item.material() == null ? null : item.material().getCode())
            .fit(item.fit() == null ? null : item.fit().getCode())
            .style(item.style() == null ? null : item.style().getCode())
            .season(null)
            .tempMin(item.tempMin())
            .tempMax(item.tempMax())
            .build();

        recommendationMapper.upsertProduct(productDto);
        return recommendationMapper.findProductIdBySourceAndExternalId(MAIN_ITEM_PRODUCT_SOURCE, externalId);
    }

    private Long upsertTop1Product(
        CategoryType categoryType,
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        SearchedProduct top1Product = EffectiveProductSelector.extractTop1Product(categoryType, effectiveProducts);
        if (top1Product == null || top1Product.marketplaceProductId() == null || top1Product.marketplaceProductId().isBlank()) {
            return null;
        }

        ProductCategoryCode productCategoryCode = ProductCategoryCode.fromSlotType(categoryType);
        Long categoryId = recommendationMapper.findCategoryIdByCode(productCategoryCode.getCode());
        if (categoryId == null) {
            return null;
        }

        var itemInfo = normalizedBlueprint.itemBySlot(categoryType);
        Integer tempMin = null;
        Integer tempMax = null;
        if (itemInfo != null && itemInfo.tempRange() != null && itemInfo.tempRange().size() == 2) {
            tempMin = itemInfo.tempRange().get(0);
            tempMax = itemInfo.tempRange().get(1);
        }

        String source = top1Product.marketplaceProvider() == null || top1Product.marketplaceProvider().isBlank()
            ? DEFAULT_PRODUCT_SOURCE
            : top1Product.marketplaceProvider();

        ProductDto productDto = ProductDto.builder()
            .source(source)
            .externalId(top1Product.marketplaceProductId())
            .categoryId(categoryId)
            .gender(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().gender() == null
                ? null
                : itemInfo.attributes().gender().getCode())
            .name(top1Product.productName())
            .brand(top1Product.brandName())
            .price(top1Product.salePrice())
            .imageUrl(top1Product.productImageUrl())
            .link(top1Product.productDetailUrl())
            .color(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().color() == null
                ? null
                : itemInfo.attributes().color().getCode())
            .material(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().material() == null
                ? null
                : itemInfo.attributes().material().getCode())
            .fit(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().fit() == null
                ? null
                : itemInfo.attributes().fit().getCode())
            .style(itemInfo == null || itemInfo.attributes() == null || itemInfo.attributes().style() == null
                ? null
                : itemInfo.attributes().style().getCode())
            .tempMin(tempMin)
            .tempMax(tempMax)
            .build();

        recommendationMapper.upsertProduct(productDto);
        return recommendationMapper.findProductIdBySourceAndExternalId(source, top1Product.marketplaceProductId());
    }

    private Long upsertProductFromCoordinationItem(
        CategoryType categoryType,
        CoordinationItemOutputDto item
    ) {
        if (
            item == null ||
            item.isMyItem() ||
            item.marketplaceProductId() == null ||
            item.marketplaceProductId().isBlank()
        ) {
            return null;
        }

        ProductCategoryCode productCategoryCode = ProductCategoryCode.fromSlotType(categoryType);
        Long categoryId = recommendationMapper.findCategoryIdByCode(productCategoryCode.getCode());
        if (categoryId == null) {
            return null;
        }

        String source = item.marketplaceProvider() == null || item.marketplaceProvider().isBlank()
            ? DEFAULT_PRODUCT_SOURCE
            : item.marketplaceProvider();

        ProductDto productDto = ProductDto.builder()
            .source(source)
            .externalId(item.marketplaceProductId())
            .categoryId(categoryId)
            .gender(null)
            .name(item.itemName())
            .brand(item.brandName())
            .price(item.salePrice())
            .imageUrl(item.imageUrl())
            .link(item.productDetailUrl())
            .color(item.color() == null ? null : item.color().getCode())
            .material(item.material() == null ? null : item.material().getCode())
            .fit(item.fit() == null ? null : item.fit().getCode())
            .style(item.style() == null ? null : item.style().getCode())
            .tempMin(item.tempMin())
            .tempMax(item.tempMax())
            .build();

        recommendationMapper.upsertProduct(productDto);
        return recommendationMapper.findProductIdBySourceAndExternalId(source, item.marketplaceProductId());
    }

    // slotSearchQueries 에서 해당 슬롯의 검색 키워드를 추출한다.
    private String extractSearchQuery(
        CategoryType categoryType,
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        if (slotSearchQueries == null) {
            return "";
        }
        ShoppingSearchQuery query = slotSearchQueries.get(categoryType);
        return query == null ? "" : query.searchKeyword();
    }

    private String extractPriority(CategoryType categoryType, NormalizedBlueprintDto normalizedBlueprint) {
        var itemInfo = normalizedBlueprint == null ? null : normalizedBlueprint.itemBySlot(categoryType);
        if (itemInfo == null || itemInfo.priority() == null) {
            return null;
        }
        return itemInfo.priority().getCode();
    }

    private String extractReason(CategoryType categoryType, NormalizedBlueprintDto normalizedBlueprint) {
        var itemInfo = normalizedBlueprint == null ? null : normalizedBlueprint.itemBySlot(categoryType);
        return itemInfo == null ? "" : itemInfo.reasoning();
    }

    private String resolveMainItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return DEFAULT_MAIN_ITEM_NAME;
        }
        return itemName;
    }

    private String extractAiExplanation(NormalizedBlueprintDto normalizedBlueprint) {
        if (normalizedBlueprint == null || normalizedBlueprint.aiBlueprint() == null) {
            return "";
        }
        String aiExplanation = normalizedBlueprint.aiBlueprint().aiExplanation();
        return aiExplanation == null ? "" : aiExplanation;
    }

    private CoordinationItemOutputDto findCoordinationItem(
        RecommendationDebugResponseDto debugResult,
        CategoryType categoryType
    ) {
        if (debugResult.coordination() == null || debugResult.coordination().isEmpty()) {
            return null;
        }

        for (CoordinationItemOutputDto item : debugResult.coordination()) {
            if (item != null && item.slotKey() == categoryType) {
                return item;
            }
        }
        return null;
    }

    private String extractSearchQuery(CategoryType categoryType, RecommendationDebugResponseDto debugResult) {
        if (debugResult.slotSearchQueries() == null || debugResult.slotSearchQueries().isEmpty()) {
            return "";
        }
        String query = debugResult.slotSearchQueries().get(categoryType.getCode());
        return query == null ? "" : query;
    }

    private String toJson(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException exception) {
            return EMPTY_JSON;
        }
    }
}

