package com.finalproject.coordi.recommendation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finalproject.coordi.recommendation.dao.ProductDao;
import com.finalproject.coordi.recommendation.dao.RecommendationDao;
import com.finalproject.coordi.recommendation.dao.RecommendationItemDao;

@Mapper
public interface RecommendationMapper {
    // recommendation 헤더 레코드를 저장한다.
    void insertRecommendation(RecommendationDao record);

    // recommendation_item 슬롯 레코드를 저장한다.
    void insertRecommendationItem(RecommendationItemDao record);

    // product 레코드를 upsert한다.
    void upsertProduct(ProductDao record);

    // source + externalId로 product_id를 조회한다.
    Long findProductIdBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    // source + externalId로 product 레코드를 조회한다.
    ProductDao findProductBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);
}
