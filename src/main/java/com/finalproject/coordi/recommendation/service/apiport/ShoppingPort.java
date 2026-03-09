package com.finalproject.coordi.recommendation.service.apiport;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 쇼핑 검색 시스템을 호출하기 위한 아웃바운드 포트.
 */
public interface ShoppingPort {

    // 검색어와 제한 수를 받아 상품 후보 목록을 반환한다.
    List<ShoppingProductCandidate> search(ShoppingSearchQuery query);

    // 쇼핑 검색 요청 파라미터를 담는 값 객체
    public record ShoppingSearchQuery(
        @NotBlank String searchKeyword,
        @Min(1) @Max(50) int resultLimit
    ) {
    }

    // 외부 쇼핑 API에서 가져온 상품 후보 데이터
    public record ShoppingProductCandidate(
        String marketplaceProvider,
        String marketplaceProductId,
        String productName,
        String brandName,
        int salePrice,
        String productImageUrl,
        String productDetailUrl
    ) {
    }
}


