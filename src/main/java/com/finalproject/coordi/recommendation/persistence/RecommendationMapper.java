package com.finalproject.coordi.recommendation.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecommendationMapper {
    // recommendation 헤더 레코드를 저장한다.
    void insertRecommendation(RecommendationRecord record);

    // recommendation_item 슬롯 레코드를 저장한다.
    void insertRecommendationItem(RecommendationItemRecord record);

    // product 레코드를 upsert한다.
    void upsertProduct(ProductRecord record);

    // source + externalId로 product_id를 조회한다.
    Long findProductIdBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);
}
