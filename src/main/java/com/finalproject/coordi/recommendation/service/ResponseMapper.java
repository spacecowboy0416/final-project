package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.domain.RecommendationResult;
import com.finalproject.coordi.recommendation.domain.ScoredRecommendationItem;
import com.finalproject.coordi.recommendation.domain.type.SelectionStage;
import com.finalproject.coordi.recommendation.dto.CoordinationOutput;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import com.finalproject.coordi.recommendation.dto.RecommendationResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {
    // 도메인 결과를 API 응답 DTO로 변환한다.
    public RecommendationResponse toResponse(RecommendationResult result, RecommendationRequest request) {
        List<CoordinationOutput> coordination = result.scoredItems().stream()
            .map(this::toCoordinationOutput)
            .toList();
        List<RecommendationResponse.ItemResult> finalItems = result.scoredItems().stream()
            .filter(item -> SelectionStage.FINAL.equals(item.selectionStage()))
            .map(this::toItemResult)
            .toList();

        return new RecommendationResponse(
            String.valueOf(result.recId()),
            result.status(),
            result.draftSource(),
            result.tpoType(),
            result.styleMode(),
            request.weather(),
            result.aiBlueprint(),
            coordination,
            finalItems
        );
    }

    // 도메인 슬롯 결과를 팀 공통 coordination 출력 모델로 변환한다.
    private CoordinationOutput toCoordinationOutput(ScoredRecommendationItem item) {
        String color = String.valueOf(item.draftItem().attributes().getOrDefault("color", "unknown"));
        String material = String.valueOf(item.draftItem().attributes().getOrDefault("material", "unknown"));
        String fit = String.valueOf(item.draftItem().attributes().getOrDefault("fit", "unknown"));
        String style = String.valueOf(item.draftItem().attributes().getOrDefault("style", "unknown"));
        return new CoordinationOutput(
            item.draftItem().slotKey(),
            item.draftItem().itemName(),
            item.draftItem().searchQuery(),
            item.selectionStage(),
            item.matchScore(),
            color,
            material,
            fit,
            style
        );
    }

    // 도메인 슬롯 결과를 응답 슬롯 모델로 변환한다.
    private RecommendationResponse.ItemResult toItemResult(ScoredRecommendationItem item) {
        String productName = item.productSnapshot() == null ? "상품 후보 없음" : item.productSnapshot().name();
        int productPrice = item.productSnapshot() == null ? 0 : item.productSnapshot().price();
        String productImageUrl = item.productSnapshot() == null ? "" : item.productSnapshot().imageUrl();
        String productLink = item.productSnapshot() == null ? "" : item.productSnapshot().link();
        RecommendationResponse.ProductPreview product = new RecommendationResponse.ProductPreview(
            productName,
            productPrice,
            productImageUrl,
            productLink
        );
        return new RecommendationResponse.ItemResult(
            item.draftItem().slotKey(),
            item.draftItem().itemName(),
            item.draftItem().searchQuery(),
            item.finalReasoning(),
            item.draftItem().priority(),
            item.selectionStage(),
            item.matchScore(),
            product
        );
    }
}
