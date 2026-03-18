package com.finalproject.coordi.recommendation.infra.imageAnalysis;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 이미지 필터 단계의 임계치/토글 설정값을 관리한다.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation.image-filter")
public class ImageAnalysisProperties {
    private boolean enabled = true;
    private boolean personCheckEnabled = false;
    private double minPersonRatio = 0.01;
    private double minGarmentRatio = 0.40;
    private String modelPath = "models/yolov8n.onnx";
    private int inputWidth = 640;
    private int inputHeight = 640;
    private double confidenceThreshold = 0.25;
    private double nmsThreshold = 0.45;
    private int personClassId = 0;
    private List<Integer> garmentClassIds = List.of(24, 25, 26, 27, 28, 31, 32, 33);
    private boolean nonPersonGarmentFallbackEnabled = true;
}
