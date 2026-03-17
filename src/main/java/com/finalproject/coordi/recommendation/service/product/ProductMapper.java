package com.finalproject.coordi.recommendation.service.product;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductSourceType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.UserRequest;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.service.product.ShoppingPort.SearchedProduct;
import org.springframework.stereotype.Component;

/**
 * 검색 결과와 blueprint 속성을 `product` 테이블 입력 DTO로 매핑하는 전용 매퍼다.
 *
 * 이 클래스가 명시적으로 매핑하는 대상:
 * - `searchedProduct.productName/brandName/salePrice/productDetailUrl` -> `product.name/brand/price/link`
 * - `persistedImageUrl` -> `product.image_url`
 * - `validatedSlot.raw().attributes().color/material/fit/style` -> `product.color/material/fit/style`
 * - `validatedBlueprint.aiBlueprint().mainItemAnalysis().season` -> `product.season`
 */
@Component
public class ProductMapper {
    /**
     * 여러 입력을 조합해 `product` 테이블 upsert용 `ProductDto`를 만든다.
     *
     * 이 메서드는 다음 데이터를 한데 모은다.
     * - request: 사용자가 보낸 입력 정보(예: gender)
     * - validatedBlueprint: 검증을 통과한 AI 결과
     * - validatedSlot: 현재 slot(tops, shoes 등)의 검증 결과
     * - searchedProduct: 네이버 검색 결과 상품
     * - categoryId: `category` 테이블의 실제 PK
     * - persistedImageUrl: `product.image_url`에 들어갈 최종 이미지 URL
     *
     * 핵심은 validated blueprint의 attribute 값을 product 컬럼 값으로 명시적으로 변환하는 것이다.
     */
    public ProductDto toProductDto(
        UserRequest request,
        NormalizedBlueprintDto validatedBlueprint,
        RawBlueprintDto.ItemInfo validatedItem,
        SearchedProduct searchedProduct,
        Long categoryId,
        String persistedImageUrl
    ) {
        RawBlueprintDto.ItemInfo itemInfo = validatedItem;
        RawBlueprintDto.Attributes attributes = itemInfo == null ? null : itemInfo.attributes();

        // Lombok @Builder가 만든 빌더를 사용해 필요한 필드를 읽기 좋은 순서로 채운다.
        // builder 패턴은 생성자 파라미터가 많을 때 실수를 줄이고 가독성을 높여준다.
        return ProductDto.builder()
            .source(ProductSourceType.NAVER.code())
            .externalId(searchedProduct.marketplaceProductId())
            .categoryId(categoryId)
            .gender(request == null || request.gender() == null ? null : request.gender().name())
            .name(searchedProduct.productName())
            .brand(searchedProduct.brandName())
            .price(searchedProduct.salePrice() <= 0 ? null : searchedProduct.salePrice())
            .imageUrl(persistedImageUrl)
            .link(searchedProduct.productDetailUrl())
            .color(attributes == null || attributes.color() == null ? null : attributes.color().code())
            .material(attributes == null || attributes.material() == null ? null : attributes.material().code())
            .fit(attributes == null || attributes.fit() == null ? null : attributes.fit().code())
            .style(attributes == null || attributes.style() == null ? null : attributes.style().code())
            .season(readSeasonCode(validatedBlueprint))
            .tempMin(readTempRangeValue(itemInfo, 0))
            .tempMax(readTempRangeValue(itemInfo, 1))
            .build();
    }

    /**
     * 애플리케이션 enum(CategoryType)을 DB category.code 값으로 바꾼다.
     */
    public String resolveCategoryCode(CategoryType categoryType) {
        return ProductCategoryCode.fromSlotType(categoryType).code();
    }

    /**
     * validatedBlueprint 안에서 계절 코드를 읽는다.
     *
     * 중간에 null이 많기 때문에 if를 여러 번 써서 방어한다.
     * Java에서는 null을 바로 호출하면 NullPointerException이 발생하므로,
     * 이런 식으로 단계적으로 확인하는 코드는 매우 흔하다.
     */
    private String readSeasonCode(NormalizedBlueprintDto validatedBlueprint) {
        if (validatedBlueprint == null
            || validatedBlueprint.aiBlueprint() == null
            || validatedBlueprint.aiBlueprint().mainItemAnalysis() == null
            || validatedBlueprint.aiBlueprint().mainItemAnalysis().season() == null) {
            return null;
        }
        return validatedBlueprint.aiBlueprint().mainItemAnalysis().season().code();
    }

    /**
     * tempRange 리스트에서 min/max 값을 꺼낸다.
     *
     * 예를 들어 tempRange가 [10, 18]이면
     * - index 0 -> 10
     * - index 1 -> 18
     *
     * size 체크를 먼저 하는 이유는
     * 리스트 길이가 부족할 때 IndexOutOfBoundsException을 막기 위해서다.
     */
    private Integer readTempRangeValue(RawBlueprintDto.ItemInfo itemInfo, int index) {
        if (itemInfo == null || itemInfo.tempRange() == null || itemInfo.tempRange().size() <= index) {
            return null;
        }
        return itemInfo.tempRange().get(index);
    }
}
