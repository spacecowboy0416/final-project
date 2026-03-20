package com.finalproject.coordi.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataUnit;
import org.springframework.util.unit.DataSize;

/**
 * recommendation 이미지 입력 설정.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation.image")
public class RecommendationImageProperties {
    /**
     * 업로드 허용 최대 이미지 크기.
     */
    @DataSizeUnit(DataUnit.BYTES)
    private DataSize maxSize = DataSize.ofMegabytes(5);
}
