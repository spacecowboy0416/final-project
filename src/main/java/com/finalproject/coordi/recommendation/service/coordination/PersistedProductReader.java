package com.finalproject.coordi.recommendation.service.coordination;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductSourceType;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 저장이 끝난 product를 DB 기준으로 다시 읽어오는 전용 컴포넌트다.
 */
@Component
@RequiredArgsConstructor
public class PersistedProductReader {
    private final RecommendationMapper recommendationMapper;
    @Qualifier("recommendationPipelineExecutor")
    private final Executor recommendationPipelineExecutor;

    public Map<CategoryType, List<ProductDto>> readBySlot(
        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot
    ) {
        if (searchedProductsBySlot == null || searchedProductsBySlot.isEmpty()) {
            return new EnumMap<>(CategoryType.class);
        }

        Map<CategoryType, CompletableFuture<List<ProductDto>>> productFutures = new EnumMap<>(CategoryType.class);
        searchedProductsBySlot.forEach((slotKey, searchedProducts) -> productFutures.put(
            slotKey,
            CompletableFuture.supplyAsync(
                () -> readSlotProducts(searchedProducts),
                recommendationPipelineExecutor
            )
        ));

        Map<CategoryType, List<ProductDto>> persistedProductsBySlot = new EnumMap<>(CategoryType.class);
        productFutures.forEach((slotKey, future) -> persistedProductsBySlot.put(slotKey, future.join()));
        return persistedProductsBySlot;
    }

    private List<ProductDto> readSlotProducts(List<SearchedProduct> searchedProducts) {
        List<String> externalIdsInOrder = collectExternalIdsInOrder(searchedProducts);
        if (externalIdsInOrder.isEmpty()) {
            return List.of();
        }

        List<ProductDto> persistedProducts = recommendationMapper.findProductsBySourceAndExternalIds(
            ProductSourceType.NAVER.code(),
            externalIdsInOrder
        );
        if (persistedProducts == null || persistedProducts.isEmpty()) {
            return List.of();
        }

        Map<String, ProductDto> productByExternalId = new LinkedHashMap<>();
        for (ProductDto product : persistedProducts) {
            if (product != null && StringUtils.hasText(product.getExternalId())) {
                productByExternalId.putIfAbsent(product.getExternalId(), product);
            }
        }

        List<ProductDto> orderedProducts = new ArrayList<>();
        for (String externalId : externalIdsInOrder) {
            ProductDto product = productByExternalId.get(externalId);
            if (product != null) {
                orderedProducts.add(product);
            }
        }
        return orderedProducts;
    }

    private List<String> collectExternalIdsInOrder(List<SearchedProduct> searchedProducts) {
        Map<String, String> externalIds = new LinkedHashMap<>();
        if (searchedProducts == null) {
            return List.of();
        }

        for (SearchedProduct searchedProduct : searchedProducts) {
            if (searchedProduct == null || !StringUtils.hasText(searchedProduct.marketplaceProductId())) {
                continue;
            }
            externalIds.putIfAbsent(searchedProduct.marketplaceProductId(), searchedProduct.marketplaceProductId());
        }
        return new ArrayList<>(externalIds.values());
    }
}

