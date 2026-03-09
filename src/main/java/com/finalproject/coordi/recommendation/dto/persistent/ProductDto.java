package com.finalproject.coordi.recommendation.dto.persistent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * product 테이블 영속 DTO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long productId;
    private String source;
    private String externalId;
    private Long categoryId;
    private String name;
    private String brand;
    private Integer price;
    private String imageUrl;
    private String link;
}


