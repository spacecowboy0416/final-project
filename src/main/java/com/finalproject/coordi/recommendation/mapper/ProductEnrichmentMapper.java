package com.finalproject.coordi.recommendation.mapper;

import com.finalproject.coordi.recommendation.dao.ProductDao;
import com.finalproject.coordi.recommendation.domain.SearchProductSnapshot;
import com.finalproject.coordi.recommendation.domain.SearchScoredItem;
import com.finalproject.coordi.recommendation.service.outboundport.ShoppingPort.ShoppingProductCandidate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * 쇼핑 후보를 DB/응답 도메인 모델로 변환한다.
 */
@Component
public class ProductEnrichmentMapper {

    /**
     * 쇼핑 후보와 저장된 productId를 결합해 enriched 추천 아이템을 생성한다.
     */
    public SearchScoredItem withProduct(
        SearchScoredItem baseItem,
        ShoppingProductCandidate picked,
        String finalImageUrl,
        Long productId
    ) {
        SearchProductSnapshot snapshot = new SearchProductSnapshot(
            picked.productName(),
            picked.salePrice(),
            finalImageUrl,
            picked.productDetailUrl()
        );
        return new SearchScoredItem(
            baseItem.blueprintItem(),
            productId,
            baseItem.selectionStage(),
            baseItem.matchScore(),
            baseItem.styleScore(),
            baseItem.colorScore(),
            baseItem.tempScore(),
            baseItem.finalReasoning(),
            snapshot,
            baseItem.blueprintSlotNode()
        );
    }

    /**
     * 쇼핑 후보를 product 테이블 저장 모델로 변환한다.
     */
    public ProductDao toProductDao(ShoppingProductCandidate picked, Long categoryId, String finalImageUrl) {
        return ProductDao.builder()
            .source(picked.marketplaceProvider())
            .externalId(picked.marketplaceProductId())
            .categoryId(categoryId)
            .name(picked.productName())
            .brand(picked.brandName())
            .price(picked.salePrice())
            .imageUrl(finalImageUrl)
            .link(picked.productDetailUrl())
            .build();
    }

    /**
     * 상품 후보가 없는 경우 검색 링크만 담은 기본 스냅샷 아이템을 생성한다.
     */
    public SearchScoredItem withEmptyProduct(SearchScoredItem baseItem) {
        String link = "https://search.shopping.naver.com/search/all?query="
            + URLEncoder.encode(baseItem.blueprintItem().searchQuery(), StandardCharsets.UTF_8);
        SearchProductSnapshot snapshot = new SearchProductSnapshot("상품 후보 없음", 0, "", link);

        return new SearchScoredItem(
            baseItem.blueprintItem(),
            null,
            baseItem.selectionStage(),
            baseItem.matchScore(),
            baseItem.styleScore(),
            baseItem.colorScore(),
            baseItem.tempScore(),
            baseItem.finalReasoning(),
            snapshot,
            baseItem.blueprintSlotNode()
        );
    }
}
