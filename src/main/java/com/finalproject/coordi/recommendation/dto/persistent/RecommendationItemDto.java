package com.finalproject.coordi.recommendation.dto.persistent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * recommendation_item 테이블 영속 DTO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationItemDto {
    private Long recItemId;
    private Long recId;
    private String slotKey;
    private String sourceType;
    private Long closetItemId;
    private Long productId;
    private String searchQuery;
    private String priority;
    private Double matchScore;
    private String reason;
}


