package com.finalproject.coordi.recommendation.service.apiport;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * <h2>ShoppingPort (Outbound Port)</h2>
 * <p>
 * 비즈니스 로직(Core)이 외부 시스템(쇼핑 API, DB 등)에 요청을 보내기 위한 인터페이스입니다.
 * 이 포트는 '무엇을' 할 것인지만 정의하며, '어떻게' 할지는 Adapter 영역에서 구현합니다.
 * </p>
 */
public interface ShoppingPort {

    /**
     * 상품 후보 목록 조회
     * * @param query 검색 키워드 및 결과 개수 설정 정보 (ShoppingSearchQuery)
     * @return 조회된 상품 정보 리스트 (ShoppingProductCandidate)
     */
    List<ShoppingProductCandidate> search(ShoppingSearchQuery query);

    /**
     * <h3>ShoppingSearchQuery (Parameter Record)</h3>
     * <p>
     * 검색 요청 시 필요한 파라미터를 캡슐화한 불변 객체입니다.
     * Port의 파라미터로 사용되어 도메인 서비스가 외부 어댑터에 전달할 데이터를 규정합니다.
     * </p>
     * * @param searchKeyword   검색 키워드
     * @param resultLimit 한 번에 가져올 결과 개수
     */
    public record ShoppingSearchQuery(
        @NotBlank String searchKeyword,
        @Min(1) @Max(50) int resultLimit
    ) {
    }

    /**
     * <h3>ShoppingProductCandidate (Response Record)</h3>
     * <p>
     * 외부 시스템(Naver, Kakao 등)으로부터 받아온 로우(Raw) 데이터를 
     * 우리 시스템의 서비스 계층에서 사용하기 적합하게 변환한 DTO(Data Transfer Object)입니다.
     * </p>
     * @param marketplaceProvider 외부 쇼핑 제공자 식별자 (예: NAVER)
     * @param marketplaceProductId 외부 쇼핑 시스템의 상품 고유 ID
     * @param productName 상품명
     * @param brandName 브랜드명
     * @param salePrice 판매 가격
     * @param productImageUrl 상품 이미지 URL
     * @param productDetailUrl 상품 상세 페이지 링크
     */
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
