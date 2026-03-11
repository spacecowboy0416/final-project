package com.finalproject.coordi.recommendation.dto.persistent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * product_tag 테이블 영속 DTO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTagDto {
    private Long productId;
    private Long tagId;
}
