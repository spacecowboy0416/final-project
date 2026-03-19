package com.finalproject.coordi.recommendation.service.finaloutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.ShoppingSearchQuery;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private static final String SELECTION_STAGE_FINAL_OUTPUT_TOP1 = "FINAL_OUTPUT_TOP1";
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

        for (CategoryType categoryType : CategoryType.values()) {
            Long productId = upsertTop1Product(categoryType, normalizedBlueprint, effectiveProducts);
            RecommendationItemDto recommendationItemDto = RecommendationItemDto.builder()
                .recId(recId)
                .slotKey(categoryType.getCode())
                .sourceType(SOURCE_TYPE_PRODUCT)
                .productId(productId)
                .searchQuery(extractSearchQuery(categoryType, slotSearchQueries))
                .priority(extractPriority(categoryType, normalizedBlueprint))
                .selectionStage(SELECTION_STAGE_FINAL_OUTPUT_TOP1)
                .matchScore(productId == null ? 0.0d : DEFAULT_MATCH_SCORE)
                .scoringDetailsJson(buildScoringDetailsJson(categoryType, productId))
                .reason(extractReason(categoryType, normalizedBlueprint))
                .build();
            recommendationMapper.insertRecommendationItem(recommendationItemDto);
        }
    }

    private Long upsertTop1Product(
        CategoryType categoryType,
        NormalizedBlueprintDto normalizedBlueprint,
        Map<CategoryType, List<SearchedProduct>> effectiveProducts
    ) {
        SearchedProduct top1Product = extractTop1Product(categoryType, effectiveProducts);
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
            ? "NAVER"
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

    // effectiveProducts 에서 슬롯별 TOP1 상품을 추출한다.
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

    private String extractAiExplanation(NormalizedBlueprintDto normalizedBlueprint) {
        if (normalizedBlueprint == null || normalizedBlueprint.aiBlueprint() == null) {
            return "";
        }
        String aiExplanation = normalizedBlueprint.aiBlueprint().aiExplanation();
        return aiExplanation == null ? "" : aiExplanation;
    }

    private String buildScoringDetailsJson(CategoryType categoryType, Long productId) {
        Map<String, Object> scoringDetails = new LinkedHashMap<>();
        scoringDetails.put("slotKey", categoryType.getCode());
        scoringDetails.put("selectionStage", SELECTION_STAGE_FINAL_OUTPUT_TOP1);
        scoringDetails.put("productResolved", productId != null);
        return toJson(scoringDetails);
    }

    private String toJson(Object source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException exception) {
            return EMPTY_JSON;
        }
    }
}
