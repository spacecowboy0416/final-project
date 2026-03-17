package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingProductCandidate;
import com.finalproject.coordi.recommendation.service.apiport.ShoppingPort.ShoppingSearchQuery;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShoppingSearcher {
    private static final int MIN_FALLBACK_TOKENS = 2;

    private final ShoppingPort shoppingPort;

    public List<ShoppingProductCandidate> search(String searchKeyword, int resultLimit) {
        return searchWithFallback(new ShoppingSearchQuery(searchKeyword, resultLimit));
    }

    public Map<CategoryType, List<ShoppingProductCandidate>> searchAll(
        Map<CategoryType, ShoppingSearchQuery> slotSearchQueries
    ) {
        Map<CategoryType, List<ShoppingProductCandidate>> candidatesBySlot = new EnumMap<>(CategoryType.class);
        if (slotSearchQueries == null || slotSearchQueries.isEmpty()) {
            return candidatesBySlot;
        }

        slotSearchQueries.forEach((slotKey, searchQuery) -> {
            List<ShoppingProductCandidate> candidates = searchWithFallback(searchQuery);
            candidatesBySlot.put(slotKey, candidates);
            log.info(
                "shopping search completed slot={} query={} candidateCount={}",
                slotKey.code(),
                searchQuery.searchKeyword(),
                candidates == null ? 0 : candidates.size()
            );
        });
        return candidatesBySlot;
    }

    private List<ShoppingProductCandidate> searchWithFallback(ShoppingSearchQuery searchQuery) {
        if (searchQuery == null || searchQuery.searchKeyword() == null || searchQuery.searchKeyword().isBlank()) {
            return List.of();
        }

        for (String attempt : buildAttempts(searchQuery.searchKeyword())) {
            List<ShoppingProductCandidate> candidates = shoppingPort.search(
                new ShoppingSearchQuery(attempt, searchQuery.resultLimit())
            );
            if (candidates != null && !candidates.isEmpty()) {
                if (!attempt.equals(searchQuery.searchKeyword())) {
                    log.info(
                        "shopping search fallback originalQuery={} fallbackQuery={} candidateCount={}",
                        searchQuery.searchKeyword(),
                        attempt,
                        candidates.size()
                    );
                }
                return candidates;
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
