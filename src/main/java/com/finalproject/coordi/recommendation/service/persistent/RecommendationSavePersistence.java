package com.finalproject.coordi.recommendation.service.persistent;

import com.finalproject.coordi.recommendation.domain.enums.CodedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationSaveRequestDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 저장 요청을 recommendation 관련 테이블에 영속화한다.
 */
@Repository
@RequiredArgsConstructor
public class RecommendationSavePersistence {
    private static final String INPUT_MODE_TEXT = "TEXT";
    private static final String PRODUCT_OPTION_SEARCH_TOP1 = "SEARCH_TOP1";
    private static final String SOURCE_TYPE_PRODUCT = "PRODUCT";
    private static final String SOURCE_TYPE_MAIN_ITEM = "MAIN_ITEM";
    private static final String DEFAULT_PRODUCT_SOURCE = "NAVER";
    private static final String MAIN_ITEM_PRODUCT_SOURCE = "MAIN_UPLOAD";
    private static final String DEFAULT_MAIN_ITEM_NAME = "main_item";
    private static final String EMPTY_JSON = "{}";

    private final RecommendationMapper recommendationMapper;

    @Transactional
    public Long save(Long userId, RecommendationSaveRequestDto request) {
        RecommendationDto recommendationDto = RecommendationDto.builder()
            .userId(userId)
            .weatherStatus(request.weatherStatusCode())
            .inputMode(INPUT_MODE_TEXT)
            .inputText(request.naturalText())
            .productOption(PRODUCT_OPTION_SEARCH_TOP1)
            .tpoType(request.tpoType().getCode())
            .styleType(request.styleType().getCode())
            .isSaved(true)
            .aiBlueprint(EMPTY_JSON)
            .aiExplanation(request.aiExplanationOrEmpty())
            .build();
        recommendationMapper.insertRecommendation(recommendationDto);

        Long recId = recommendationDto.getRecId();
        if (recId == null) {
            return null;
        }

        for (CoordinationItemOutputDto coordinationItem : request.persistableCoordination()) {
            Long productId = upsertProduct(coordinationItem);
            if (productId == null) {
                continue;
            }

            recommendationMapper.insertRecommendationItem(RecommendationItemDto.builder()
                .recId(recId)
                .slotKey(coordinationItem.slotKey().getCode())
                .sourceType(coordinationItem.isMyItem() ? SOURCE_TYPE_MAIN_ITEM : SOURCE_TYPE_PRODUCT)
                .productId(productId)
                .searchQuery(request.searchQueryOf(coordinationItem.slotKey()))
                .priority(coordinationItem.priority() == null ? null : coordinationItem.priority().getCode())
                .matchScore(coordinationItem.matchScore())
                .reason(coordinationItem.reasoning())
                .build());
        }

        return recId;
    }

    private Long upsertProduct(CoordinationItemOutputDto coordinationItem) {
        Long categoryId = resolveCategoryId(coordinationItem.slotKey());
        if (categoryId == null) {
            return null;
        }

        ProductDto productDto;
        if (coordinationItem.isMyItem()) {
            productDto = ProductDto.builder()
                .source(MAIN_ITEM_PRODUCT_SOURCE)
                .externalId(UUID.randomUUID().toString())
                .categoryId(categoryId)
                .name(resolveMainItemName(coordinationItem.itemName()))
                .color(toCode(coordinationItem.color()))
                .material(toCode(coordinationItem.material()))
                .fit(toCode(coordinationItem.fit()))
                .style(toCode(coordinationItem.style()))
                .tempMin(coordinationItem.tempMin())
                .tempMax(coordinationItem.tempMax())
                .build();
        } else {
            if (coordinationItem.marketplaceProductId() == null || coordinationItem.marketplaceProductId().isBlank()) {
                return null;
            }
            productDto = ProductDto.builder()
                .source(resolveProductSource(coordinationItem.marketplaceProvider()))
                .externalId(coordinationItem.marketplaceProductId())
                .categoryId(categoryId)
                .name(coordinationItem.itemName())
                .brand(coordinationItem.brandName())
                .price(coordinationItem.salePrice())
                .imageUrl(coordinationItem.imageUrl())
                .link(coordinationItem.productDetailUrl())
                .color(toCode(coordinationItem.color()))
                .material(toCode(coordinationItem.material()))
                .fit(toCode(coordinationItem.fit()))
                .style(toCode(coordinationItem.style()))
                .tempMin(coordinationItem.tempMin())
                .tempMax(coordinationItem.tempMax())
                .build();
        }

        recommendationMapper.upsertProduct(productDto);
        return productDto.getProductId();
    }

    private Long resolveCategoryId(CategoryType slotKey) {
        try {
            return recommendationMapper.findCategoryIdByCode(ProductCategoryCode.fromSlotType(slotKey));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String toCode(CodedEnum codedEnum) {
        return codedEnum == null ? null : codedEnum.getCode();
    }

    private String resolveMainItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return DEFAULT_MAIN_ITEM_NAME;
        }
        return itemName;
    }

    private String resolveProductSource(String marketplaceProvider) {
        if (marketplaceProvider == null || marketplaceProvider.isBlank()) {
            return DEFAULT_PRODUCT_SOURCE;
        }
        return marketplaceProvider;
    }
}
