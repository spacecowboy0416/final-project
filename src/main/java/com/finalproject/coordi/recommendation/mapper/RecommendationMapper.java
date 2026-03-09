package com.finalproject.coordi.recommendation.mapper;

import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * recommendation 도메인 MyBatis 매퍼 계약.
 */
@Mapper
public interface RecommendationMapper {
    int insertRecommendation(RecommendationDto recommendation);

    int insertRecommendationItem(RecommendationItemDto recommendationItem);

    int upsertProduct(ProductDto product);

    Long findProductIdBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    ProductDto findProductBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);
}

