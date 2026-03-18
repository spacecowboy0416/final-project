package com.finalproject.coordi.recommendation.infra.imageAnalysis;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.finalproject.coordi.recommendation.service.imagefilter.ImageAnalysisPort;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import static org.bytedeco.opencv.global.opencv_core.CV_8U;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * YOLO 기반 이미지 분석 어댑터.
 * 현재는 런타임 연동 전 단계이므로 최소 분석 결과만 반환한다.
 */
@Component
@RequiredArgsConstructor
public class YoloImageAnalysisAdapter implements ImageAnalysisPort {
    private final ImageAnalysisProperties imageFilterProperties;
    private final Object sessionLock = new Object();
    private volatile OrtSession cachedSession;

    @Override
    public ImageAnalysisResult analyze(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return new ImageAnalysisResult(false, 0.0, 0.0);
        }

        ModelContext modelContext = loadModel();
        PreprocessedImage preprocessedImage = preprocess(imageUrl, modelContext);
        InferenceOutput inferenceOutput = infer(preprocessedImage, modelContext);
        return postprocess(inferenceOutput, modelContext);
    }

    private ModelContext loadModel() {
        // TODO: ONNX Runtime 세션 생성 및 모델 로드를 구현한다.
        return new ModelContext(
            imageFilterProperties.getModelPath(),
            imageFilterProperties.getInputWidth(),
            imageFilterProperties.getInputHeight(),
            imageFilterProperties.getConfidenceThreshold(),
            imageFilterProperties.getNmsThreshold(),
            imageFilterProperties.getPersonClassId(),
            new HashSet<>(imageFilterProperties.getGarmentClassIds()),
            imageFilterProperties.isNonPersonGarmentFallbackEnabled()
        );
    }

    private PreprocessedImage preprocess(String imageUrl, ModelContext modelContext) {
        try {
            byte[] imageBytes = downloadImageBytes(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                return new PreprocessedImage(imageUrl, false, 0, 0, new float[0]);
            }
            try (
                BytePointer imagePointer = new BytePointer(imageBytes);
                Mat imageBuffer = new Mat(1, imageBytes.length, CV_8U, imagePointer);
                Mat originalImage = imdecode(imageBuffer, IMREAD_COLOR);
                Mat resizedImage = new Mat()
            ) {
                if (originalImage == null || originalImage.empty()) {
                    return new PreprocessedImage(imageUrl, false, 0, 0, new float[0]);
                }

                resize(
                    originalImage,
                    resizedImage,
                    new Size(modelContext.inputWidth(), modelContext.inputHeight())
                );
                float[] chwTensorData = toChwTensorData(resizedImage);
                return new PreprocessedImage(
                    imageUrl,
                    true,
                    resizedImage.cols(),
                    resizedImage.rows(),
                    chwTensorData
                );
            }
        } catch (Exception exception) {
            return new PreprocessedImage(imageUrl, false, 0, 0, new float[0]);
        }
    }

    private InferenceOutput infer(PreprocessedImage preprocessedImage, ModelContext modelContext) {
        if (!preprocessedImage.preprocessed()) {
            return new InferenceOutput(false, 0.0, 0.0);
        }
        try {
            return runInference(preprocessedImage, modelContext);
        } catch (Exception exception) {
            return new InferenceOutput(false, 0.0, 0.0);
        }
    }

    private ImageAnalysisResult postprocess(InferenceOutput inferenceOutput, ModelContext modelContext) {
        if (!inferenceOutput.inferenceReady()) {
            return new ImageAnalysisResult(false, 0.0, 0.0);
        }
        return new ImageAnalysisResult(
            true,
            inferenceOutput.personRatio(),
            inferenceOutput.garmentRatio()
        );
    }

    private byte[] downloadImageBytes(String imageUrl) throws Exception {
        try (InputStream imageStream = new URL(imageUrl).openStream()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private InferenceOutput runInference(PreprocessedImage preprocessedImage, ModelContext modelContext) throws Exception {
        OrtSession session = getOrCreateSession(modelContext.modelPath());
        if (session.getInputNames().isEmpty()) {
            throw new OrtException("ONNX model input not found.");
        }

        String inputName = session.getInputNames().iterator().next();
        long[] inputShape = new long[] {1, 3, modelContext.inputHeight(), modelContext.inputWidth()};
        float[] inputData = preprocessedImage.chwTensorData();
        if (inputData == null || inputData.length == 0) {
            throw new OrtException("ONNX input tensor data is empty.");
        }

        try (OnnxTensor inputTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), FloatBuffer.wrap(inputData), inputShape);
            OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {
            return toInferenceOutput(result, modelContext);
        }
    }

    private InferenceOutput toInferenceOutput(OrtSession.Result result, ModelContext modelContext) throws OrtException {
        if (result == null || result.size() == 0 || result.get(0) == null) {
            return new InferenceOutput(false, 0.0, 0.0);
        }

        Object outputValue = result.get(0).getValue();
        if (outputValue instanceof float[][][] output3d) {
            return parseYoloOutput3d(output3d, modelContext);
        }
        if (outputValue instanceof float[][] output2d) {
            return parseYoloOutput2d(output2d, modelContext);
        }
        return new InferenceOutput(false, 0.0, 0.0);
    }

    private InferenceOutput parseYoloOutput3d(float[][][] output3d, ModelContext modelContext) {
        if (output3d.length == 0) {
            return new InferenceOutput(false, 0.0, 0.0);
        }
        float[][] matrix = output3d[0];
        if (matrix.length < 6) {
            return new InferenceOutput(false, 0.0, 0.0);
        }

        boolean attrsFirst = matrix.length < matrix[0].length;
        return attrsFirst
            ? computeRatiosAttrsFirst(matrix, modelContext)
            : computeRatiosBoxesFirst(matrix, modelContext);
    }

    private InferenceOutput parseYoloOutput2d(float[][] output2d, ModelContext modelContext) {
        if (output2d.length < 6) {
            return new InferenceOutput(false, 0.0, 0.0);
        }
        boolean attrsFirst = output2d.length < output2d[0].length;
        return attrsFirst
            ? computeRatiosAttrsFirst(output2d, modelContext)
            : computeRatiosBoxesFirst(output2d, modelContext);
    }

    private InferenceOutput computeRatiosAttrsFirst(float[][] attrsFirstMatrix, ModelContext modelContext) {
        int attrs = attrsFirstMatrix.length;
        int boxes = attrsFirstMatrix[0].length;
        if (attrs < 6 || boxes == 0) {
            return new InferenceOutput(false, 0.0, 0.0);
        }

        double personArea = 0.0;
        double garmentArea = 0.0;
        int classCount = attrs - 4;
        for (int boxIndex = 0; boxIndex < boxes; boxIndex++) {
            float width = attrsFirstMatrix[2][boxIndex];
            float height = attrsFirstMatrix[3][boxIndex];
            ClassScore classScore = findBestClass(attrsFirstMatrix, boxIndex, classCount);
            if (classScore.score() < modelContext.confidenceThreshold()) {
                continue;
            }

            double boxArea = clampPositive(width) * clampPositive(height);
            if (isPersonClassId(classScore.classId(), modelContext)) {
                personArea += boxArea;
            } else if (isGarmentClassId(classScore.classId(), modelContext)) {
                garmentArea += boxArea;
            }
        }
        return toRatioOutput(personArea, garmentArea, modelContext);
    }

    private InferenceOutput computeRatiosBoxesFirst(float[][] boxesFirstMatrix, ModelContext modelContext) {
        if (boxesFirstMatrix.length == 0 || boxesFirstMatrix[0].length < 6) {
            return new InferenceOutput(false, 0.0, 0.0);
        }

        double personArea = 0.0;
        double garmentArea = 0.0;
        int classCount = boxesFirstMatrix[0].length - 4;
        for (float[] row : boxesFirstMatrix) {
            float width = row[2];
            float height = row[3];
            ClassScore classScore = findBestClass(row, classCount);
            if (classScore.score() < modelContext.confidenceThreshold()) {
                continue;
            }

            double boxArea = clampPositive(width) * clampPositive(height);
            if (isPersonClassId(classScore.classId(), modelContext)) {
                personArea += boxArea;
            } else if (isGarmentClassId(classScore.classId(), modelContext)) {
                garmentArea += boxArea;
            }
        }
        return toRatioOutput(personArea, garmentArea, modelContext);
    }

    private ClassScore findBestClass(float[][] attrsFirstMatrix, int boxIndex, int classCount) {
        int bestClassId = -1;
        float bestScore = 0.0f;
        for (int classOffset = 0; classOffset < classCount; classOffset++) {
            float score = attrsFirstMatrix[4 + classOffset][boxIndex];
            if (score > bestScore) {
                bestScore = score;
                bestClassId = classOffset;
            }
        }
        return new ClassScore(bestClassId, bestScore);
    }

    private ClassScore findBestClass(float[] boxRow, int classCount) {
        int bestClassId = -1;
        float bestScore = 0.0f;
        for (int classOffset = 0; classOffset < classCount; classOffset++) {
            float score = boxRow[4 + classOffset];
            if (score > bestScore) {
                bestScore = score;
                bestClassId = classOffset;
            }
        }
        return new ClassScore(bestClassId, bestScore);
    }

    private InferenceOutput toRatioOutput(double personArea, double garmentArea, ModelContext modelContext) {
        double imageArea = (double) modelContext.inputWidth() * modelContext.inputHeight();
        if (imageArea <= 0.0) {
            return new InferenceOutput(false, 0.0, 0.0);
        }
        double personRatio = clampRatio(personArea / imageArea);
        double garmentRatio = clampRatio(garmentArea / imageArea);
        return new InferenceOutput(true, personRatio, garmentRatio);
    }

    private boolean isPersonClassId(int classId, ModelContext modelContext) {
        return classId == modelContext.personClassId();
    }

    private boolean isGarmentClassId(int classId, ModelContext modelContext) {
        if (modelContext.garmentClassIds().contains(classId)) {
            return true;
        }
        return modelContext.nonPersonGarmentFallbackEnabled() && classId >= 0 && classId != modelContext.personClassId();
    }

    private double clampPositive(float value) {
        return Math.max(0.0, value);
    }

    private double clampRatio(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private float[] toChwTensorData(Mat resizedImage) {
        int height = resizedImage.rows();
        int width = resizedImage.cols();
        int channels = resizedImage.channels();
        if (channels < 3) {
            return new float[0];
        }

        float[] chwTensorData = new float[3 * height * width];
        try (UByteIndexer indexer = resizedImage.createIndexer()) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // OpenCV 기본 BGR 순서를 YOLO 입력 RGB 순서로 변환한다.
                    float b = (indexer.get(y, x, 0) & 0xFF) / 255.0f;
                    float g = (indexer.get(y, x, 1) & 0xFF) / 255.0f;
                    float r = (indexer.get(y, x, 2) & 0xFF) / 255.0f;

                    int pixelIndex = y * width + x;
                    chwTensorData[pixelIndex] = r;
                    chwTensorData[(height * width) + pixelIndex] = g;
                    chwTensorData[(2 * height * width) + pixelIndex] = b;
                }
            }
        }
        return chwTensorData;
    }

    private OrtSession getOrCreateSession(String modelPath) throws Exception {
        if (cachedSession != null) {
            return cachedSession;
        }
        synchronized (sessionLock) {
            if (cachedSession != null) {
                return cachedSession;
            }
            byte[] modelBytes = readModelBytes(modelPath);
            cachedSession = OrtEnvironment.getEnvironment().createSession(modelBytes, new OrtSession.SessionOptions());
            return cachedSession;
        }
    }

    private byte[] readModelBytes(String modelPath) throws Exception {
        if (StringUtils.hasText(modelPath) && Files.exists(Path.of(modelPath))) {
            return Files.readAllBytes(Path.of(modelPath));
        }

        String normalizedPath = modelPath != null && modelPath.startsWith("/")
            ? modelPath.substring(1)
            : modelPath;
        try (InputStream modelStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalizedPath)) {
            if (modelStream == null) {
                throw new IllegalStateException("ONNX model not found: " + modelPath);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            modelStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private record ModelContext(
        String modelPath,
        int inputWidth,
        int inputHeight,
        double confidenceThreshold,
        double nmsThreshold,
        int personClassId,
        Set<Integer> garmentClassIds,
        boolean nonPersonGarmentFallbackEnabled
    ) {
    }

    private record PreprocessedImage(
        String imageUrl,
        boolean preprocessed,
        int width,
        int height,
        float[] chwTensorData
    ) {
    }

    private record InferenceOutput(
        boolean inferenceReady,
        double personRatio,
        double garmentRatio
    ) {
    }

    private record ClassScore(
        int classId,
        float score
    ) {
    }
}
