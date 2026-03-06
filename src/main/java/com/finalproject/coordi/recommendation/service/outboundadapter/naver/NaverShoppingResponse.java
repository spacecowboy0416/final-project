package com.finalproject.coordi.recommendation.service.outboundadapter.naver;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.service.outboundport.ShoppingPort.ShoppingProductCandidate;
import java.util.ArrayList;
import java.util.List;

/**
 * 네이버 쇼핑 API 원본 응답을 보관하고 포트 표준 모델(ShoppingProductCandidate)로 변환하는 네이버 전용 DTO다.
 */
public record NaverShoppingResponse(
    List<NaverShoppingItem> items
) {
    private static final String NAVER_PROVIDER = "NAVER";

    public static NaverShoppingResponse fromJson(JsonNode rootNode) {
        JsonNode itemsNode = rootNode.path("items");
        if (!itemsNode.isArray()) {
            return new NaverShoppingResponse(List.of());
        }

        List<NaverShoppingItem> parsedItems = new ArrayList<>();
        for (JsonNode itemNode : itemsNode) {
            parsedItems.add(
                new NaverShoppingItem(
                    text(itemNode, "productId"),
                    text(itemNode, "title"),
                    text(itemNode, "brand"),
                    text(itemNode, "lprice"),
                    text(itemNode, "image"),
                    text(itemNode, "link")
                )
            );
        }
        return new NaverShoppingResponse(parsedItems);
    }

    public List<ShoppingProductCandidate> toCandidates() {
        List<ShoppingProductCandidate> candidates = new ArrayList<>();
        for (NaverShoppingItem item : items) {
            candidates.add(
                new ShoppingProductCandidate(
                    NAVER_PROVIDER,
                    item.productId(),
                    NaverShoppingUtils.stripHtml(item.title()),
                    item.brand(),
                    NaverShoppingUtils.parsePrice(item.lowPrice()),
                    item.imageUrl(),
                    item.productUrl()
                )
            );
        }
        return candidates;
    }

    private static String text(JsonNode node, String key) {
        JsonNode valueNode = node.path(key);
        return valueNode.isMissingNode() || valueNode.isNull() ? "" : valueNode.asText("");
    }

    public record NaverShoppingItem(
        String productId,
        String title,
        String brand,
        String lowPrice,
        String imageUrl,
        String productUrl
    ) {
    }
}
