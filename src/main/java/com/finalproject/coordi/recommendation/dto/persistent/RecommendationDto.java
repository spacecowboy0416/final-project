package com.finalproject.coordi.recommendation.dto.persistent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * recommendation 테이블 영속 DTO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDto {
    private Long recId;
    private Long userId;
    private String inputMode;
    private String inputText;
    private String productOption;
    private String tpoType;
    private String styleType;
    private Boolean isSaved;
    private String aiBlueprint;
    private String aiExplanation;
}


