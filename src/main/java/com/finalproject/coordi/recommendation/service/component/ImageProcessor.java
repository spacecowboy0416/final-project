package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintRequestDto;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * recommendation 파이프라인에서 Gemini 호출 직전에 이미지 입력을 정규화하는 컴포넌트.
 * 사용 위치:
 * - {@code GeminiAiAdapter}: generateContent 요청의 inlineData 생성 직전
 *
 * 실패 정책:
 * - 이미지 해석/인코딩 실패 시 recommendation 흐름을 끊지 않기 위해 원본으로 폴백한다.
 */
@Slf4j
@Component
public class ImageProcessor {
    @Value("${external.api.gemini.image.max-long-edge:1280}")
    private int maxLongEdge;

    @Value("${external.api.gemini.image.jpeg-quality:0.82}")
    private float jpegQuality;

    /**
     * Gemini 전송용 이미지를 정책에 맞게 가공한다.
     *
     * @param original recommendation 요청이 전달한 원본 이미지 데이터
     * @return Gemini 전송에 사용할 이미지 데이터(성공 시 JPEG, 실패 시 원본)
     */
    public ProcessedImage process(BlueprintRequestDto.ImageData original) {
        if (original == null || original.imageBytes() == null || original.imageBytes().length == 0) {
            return ProcessedImage.fallback(original, "input-missing");
        }

        try {
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(original.imageBytes()));
            if (inputImage == null) {
                return ProcessedImage.fallback(original, "decode-failed");
            }

            BufferedImage resizedImage = resizeByLongEdge(inputImage, maxLongEdge);
            byte[] encodedBytes = encodeJpeg(resizedImage, jpegQuality);
            if (encodedBytes.length == 0) {
                return ProcessedImage.fallback(original, "encode-empty");
            }

            boolean resized = resizedImage.getWidth() != inputImage.getWidth()
                || resizedImage.getHeight() != inputImage.getHeight();
            return new ProcessedImage("image/jpeg", encodedBytes, resized, false, "processed");
        } catch (Exception exception) {
            log.warn("image processing failed. fallback to original image.", exception);
            return ProcessedImage.fallback(original, "exception");
        }
    }

    private BufferedImage resizeByLongEdge(BufferedImage source, int maxLongEdgeValue) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int longEdge = Math.max(sourceWidth, sourceHeight);
        if (longEdge <= maxLongEdgeValue) {
            return toRgbImage(source);
        }

        double scale = (double) maxLongEdgeValue / (double) longEdge;
        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));

        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return target;
    }

    private BufferedImage toRgbImage(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return rgb;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws Exception {
        float clampedQuality = Math.max(0.1f, Math.min(1.0f, quality));
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageWriter available");
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(buffer)) {
            writer.setOutput(output);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(clampedQuality);
            }
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
        return buffer.toByteArray();
    }

    /**
     * 전처리 결과를 호출자에게 전달하는 불변 모델이다.
     * fallback 여부를 함께 제공해 호출자가 관측 로그를 일관된 형식으로 남길 수 있게 한다.
     */
    public record ProcessedImage(
        String mimeType,
        byte[] imageBytes,
        boolean resized,
        boolean fallback,
        String reason
    ) {
        private static ProcessedImage fallback(BlueprintRequestDto.ImageData original, String reason) {
            if (original == null) {
                return new ProcessedImage("image/jpeg", new byte[0], false, true, reason);
            }
            byte[] fallbackBytes = original.imageBytes() == null ? new byte[0] : original.imageBytes();
            return new ProcessedImage(original.mimeType(), fallbackBytes, false, true, reason);
        }
    }
}
