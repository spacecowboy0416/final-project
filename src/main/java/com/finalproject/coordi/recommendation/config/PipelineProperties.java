package com.finalproject.coordi.recommendation.config;

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
public class PipelineProperties {
    /**
     * true면 FAST_TOP1 경로(이미지 필터/코디네이션 생략),
     * false면 LEGACY_FULL 파이프라인(Noop)을 사용한다.
     */
    private boolean fastTop1Enabled = true;
}
