package com.finalproject.coordi.recommendation.service.imagefilter;

/**
 * 이미지 분석 엔진(OpenCV/ONNX 등)을 추상화한 아웃바운드 포트다.
 */
public interface ImageAnalysisPort {

    // 이미지 URL을 분석해 사람/의류 비율 신호를 반환한다.
    ImageAnalysisResult analyze(String imageUrl);

    record ImageAnalysisResult(
        boolean analyzable,
        double personRatio,
        double garmentRatio
    ) {
    }
}
