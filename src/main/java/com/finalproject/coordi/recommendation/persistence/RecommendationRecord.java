package com.finalproject.coordi.recommendation.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * recommendation 테이블 영속 모델.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRecord {
    private Long recId;
    private Long userId;
    private String inputMode;
    private String inputText;
    private String productOption;
    private Boolean isSaved;
    private String aiBlueprint;
    private String aiExplanation;
}
