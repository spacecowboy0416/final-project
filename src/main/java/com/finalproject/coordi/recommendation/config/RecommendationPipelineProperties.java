package com.finalproject.coordi.recommendation.config;

import com.finalproject.coordi.recommendation.domain.enums.PipelineMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * recommendation 파이프라인 실행 모드 설정.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation.pipeline")
public class RecommendationPipelineProperties {
    private PipelineMode mode = PipelineMode.LEGACY_FULL;
}
