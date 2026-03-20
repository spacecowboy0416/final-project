package com.finalproject.coordi.recommendation.mapper;

import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductImageMetadataDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductTagDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemTagDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * recommendation 도메인 MyBatis 매퍼 계약.
 */
@Mapper
public interface RecommendationMapper {
    int insertRecommendation(RecommendationDto recommendation);

    int insertRecommendationItem(RecommendationItemDto recommendationItem);

    int insertRecommendationItemTag(RecommendationItemTagDto recommendationItemTag);

    int upsertProduct(ProductDto product);

    int insertProductImageMetadata(ProductImageMetadataDto productImageMetadata);

    int insertProductTag(ProductTagDto productTag);

    Long findCategoryIdByCode(@Param("code") String code);

    Long findProductIdBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    ProductDto findProductBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    Long findClosetItemIdByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    int insertClosetItem(@Param("userId") Long userId, @Param("productId") Long productId);

    int updateRecommendationItemClosetItemId(@Param("recItemId") Long recItemId, @Param("closetItemId") Long closetItemId);

    List<ProductDto> findProductsBySourceAndExternalIds(
        @Param("source") String source,
        @Param("externalIds") List<String> externalIds
    );
}
