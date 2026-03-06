package com.finalproject.coordi.recommendation.service.outboundadapter.naver;

import com.finalproject.coordi.recommendation.service.outboundport.ShoppingPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ShoppingPort 구현체로서 검색 입력 검증과 네이버 전용 클라이언트/응답 DTO 조합만 담당하며, 네이버 의존 세부사항은 하위 객체로 캡슐화한다.
 */
@Component
@RequiredArgsConstructor
public class NaverShoppingAdapter implements ShoppingPort {
    private final NaverShoppingClient naverShoppingClient;

    @Override
    public List<ShoppingProductCandidate> search(ShoppingSearchQuery query) {
        String keyword = query.searchKeyword() == null ? "" : query.searchKeyword().trim();
        if (keyword.isBlank()) {
            return List.of();
        }
        return naverShoppingClient.search(keyword, query.resultLimit()).toCandidates();
    }
}
