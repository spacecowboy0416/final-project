package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.domain.ProductSnapshot;
import com.finalproject.coordi.recommendation.domain.ScoredRecommendationItem;
import com.finalproject.coordi.recommendation.domain.type.SlotKey;
import com.finalproject.coordi.recommendation.outbound.S3Port;
import com.finalproject.coordi.recommendation.outbound.S3UploadRequest;
import com.finalproject.coordi.recommendation.outbound.ShoppingPort;
import com.finalproject.coordi.recommendation.outbound.ShoppingProductCandidate;
import com.finalproject.coordi.recommendation.outbound.ShoppingSearchQuery;
import com.finalproject.coordi.recommendation.persistence.ProductRecord;
import com.finalproject.coordi.recommendation.persistence.RecommendationMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class ProductService {
    private static final Map<SlotKey, Long> SLOT_CATEGORY_ID = Map.of(
        SlotKey.TOPS, 1L,
        SlotKey.BOTTOMS, 2L,
        SlotKey.OUTERWEAR, 3L,
        SlotKey.SHOES, 4L,
        SlotKey.ACCESSORIES, 5L
    );

    private final ShoppingPort shoppingPort;
    private final S3Port s3Port;
    private final RecommendationMapper recommendationMapper;

    public ProductService(ShoppingPort shoppingPort, S3Port s3Port, RecommendationMapper recommendationMapper) {
        this.shoppingPort = shoppingPort;
        this.s3Port = s3Port;
        this.recommendationMapper = recommendationMapper;
    }

    // 슬롯별 검색을 수행하고 상품을 저장한 뒤 product_id를 연결한다.
    public List<ScoredRecommendationItem> attachProducts(List<ScoredRecommendationItem> scoredItems) {
        List<ScoredRecommendationItem> enriched = new ArrayList<>();
        for (ScoredRecommendationItem item : scoredItems) {
            ShoppingProductCandidate picked = findBestCandidate(item);
            if (picked == null) {
                enriched.add(withEmptyProduct(item));
                continue;
            }

            Long categoryId = SLOT_CATEGORY_ID.getOrDefault(item.draftItem().slotKey(), 1L);
            String s3ImageUrl = s3Port.uploadImage(
                new S3UploadRequest(picked.imageUrl(), "recommendation/product")
            );
            String finalImageUrl = (s3ImageUrl == null || s3ImageUrl.isBlank()) ? picked.imageUrl() : s3ImageUrl;
            ProductRecord record = ProductRecord.builder()
                .source(picked.source())
                .externalId(picked.externalId())
                .categoryId(categoryId)
                .name(picked.name())
                .brand(picked.brand())
                .price(picked.price())
                .imageUrl(finalImageUrl)
                .link(picked.link())
                .build();
            recommendationMapper.upsertProduct(record);
            Long productId = recommendationMapper.findProductIdBySourceAndExternalId(picked.source(), picked.externalId());

            ProductSnapshot snapshot = new ProductSnapshot(
                picked.name(),
                picked.price(),
                finalImageUrl,
                picked.link()
            );
            enriched.add(new ScoredRecommendationItem(
                item.draftItem(),
                productId,
                item.selectionStage(),
                item.matchScore(),
                item.styleScore(),
                item.colorScore(),
                item.tempScore(),
                item.finalReasoning(),
                snapshot,
                item.blueprintSlotNode()
            ));
        }
        return enriched;
    }

    // 검색 결과에서 1순위 후보를 선택한다.
    private ShoppingProductCandidate findBestCandidate(ScoredRecommendationItem item) {
        List<ShoppingProductCandidate> candidates = shoppingPort.search(
            new ShoppingSearchQuery(item.draftItem().searchQuery(), 5)
        );
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(0);
    }

    // 상품 후보가 없을 때 검색 링크만 포함한 최소 스냅샷을 생성한다.
    private ScoredRecommendationItem withEmptyProduct(ScoredRecommendationItem item) {
        String link = "https://search.shopping.naver.com/search/all?query="
            + URLEncoder.encode(item.draftItem().searchQuery(), StandardCharsets.UTF_8);
        ProductSnapshot snapshot = new ProductSnapshot(
            "상품 후보 없음",
            0,
            "",
            link
        );
        return new ScoredRecommendationItem(
            item.draftItem(),
            null,
            item.selectionStage(),
            item.matchScore(),
            item.styleScore(),
            item.colorScore(),
            item.tempScore(),
            item.finalReasoning(),
            snapshot,
            item.blueprintSlotNode()
        );
    }
}
