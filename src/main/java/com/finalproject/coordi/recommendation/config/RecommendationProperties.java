package com.finalproject.coordi.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

/**
 * recommendation 공통 설정.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {
    /**
     * true면 FAST_TOP1 경로(이미지 필터/코디네이션 생략),
     * false면 LEGACY_FULL 파이프라인(Noop)을 사용한다.
     */
    private boolean fastTop1Enabled = true;

    private DataSize maxSize = DataSize.ofMegabytes(5);

}
