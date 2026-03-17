package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.infra.naver.NaverShoppingSchemaProvider;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.ShoppingSearchQuery;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class ShoppingSearcher {
    private static final Logger log = LoggerFactory.getLogger(ShoppingSearcher.class);

    private static final int MIN_FALLBACK_TOKENS = 2;

    private final ShoppingPort shoppingPort;
    private final NaverShoppingSchemaProvider schemaProvider;
    @Qualifier("recommendationPipelineExecutor")
    private final Executor recommendationPipelineExecutor;

    public List<SearchedProduct> search(String searchKeyword) {
        return searchWithFallback(new ShoppingSearchQuery(searchKeyword, schemaProvider.resultLimit()));
    }

    public Map<CategoryType, List<SearchedProduct>> searchAll(
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        if (slotSearchQueries == null || slotSearchQueries.isEmpty()) {
            return new EnumMap<>(CategoryType.class);
        }

        Map<CategoryType, CompletableFuture<List<SearchedProduct>>> searchedProductFutures =
            new EnumMap<>(CategoryType.class);

        slotSearchQueries.forEach((slotKey, searchQuery) -> searchedProductFutures.put(
            slotKey,
            CompletableFuture.supplyAsync(
                () -> searchSlot(slotKey, searchQuery),
                recommendationPipelineExecutor
            )
        ));

        Map<CategoryType, List<SearchedProduct>> searchedProductsBySlot = new EnumMap<>(CategoryType.class);
        searchedProductFutures.forEach((slotKey, future) -> searchedProductsBySlot.put(slotKey, future.join()));
        return searchedProductsBySlot;
    }

    private List<SearchedProduct> searchSlot(
        CategoryType slotKey,
        ShoppingSearchQuery searchQuery
    ) {
        List<SearchedProduct> searchedProducts = searchWithFallback(searchQuery);
        log.info(
            "shopping search completed slot={} query={} searchedProductCount={}",
            slotKey.code(),
            searchQuery.searchKeyword(),
            searchedProducts == null ? 0 : searchedProducts.size()
        );
        return searchedProducts;
    }

    private List<SearchedProduct> searchWithFallback(ShoppingSearchQuery searchQuery) {
        if (searchQuery == null || searchQuery.searchKeyword() == null || searchQuery.searchKeyword().isBlank()) {
            return List.of();
        }

        for (String attempt : buildAttempts(searchQuery.searchKeyword())) {
            List<SearchedProduct> searchedProducts = shoppingPort.search(
                new ShoppingSearchQuery(attempt, searchQuery.resultLimit())
            );
            if (searchedProducts != null && !searchedProducts.isEmpty()) {
                if (!attempt.equals(searchQuery.searchKeyword())) {
                    log.info(
                        "shopping search fallback originalQuery={} fallbackQuery={} searchedProductCount={}",
                        searchQuery.searchKeyword(),
                        attempt,
                        searchedProducts.size()
                    );
                }
                return searchedProducts;
            }
        }
        return List.of();
    }

    private List<String> buildAttempts(String rawQuery) {
        String[] tokens = rawQuery.trim().split("\\s+");
        List<String> attempts = new ArrayList<>();
        attempts.add(String.join(" ", tokens));

        for (int tokenLength = tokens.length - 1; tokenLength >= MIN_FALLBACK_TOKENS; tokenLength--) {
            String attempt = String.join(" ", java.util.Arrays.copyOf(tokens, tokenLength));
            if (!attempts.contains(attempt)) {
                attempts.add(attempt);
            }
        }
        return attempts;
    }
}


