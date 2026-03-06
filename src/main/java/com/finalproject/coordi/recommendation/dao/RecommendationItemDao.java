package com.finalproject.coordi.recommendation.dao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * recommendation_item 테이블 영속 모델.
 */
@Getter
@Setter 
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationItemDao {
    private Long recItemId;
    private Long recId;
    private String slotKey;
    private String sourceType;
    private Long productId;
    private Long categoryId;
    private String itemName;
    private String searchQuery;
    private String attributesJson;
    private Integer tempMin;
    private Integer tempMax;
    private String priority;
    private String selectionStage;
    private Double matchScore;
    private Double styleScore;
    private Double colorScore;
    private Double tempScore;
    private String scoringDetailsJson;
    private String reason;
}
