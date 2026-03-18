package com.finalproject.coordi.recommendation.dto.persistent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * product_image_metadata 테이블 영속 DTO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageMetadataDto {
    private Long imageMetaId;
    private Long productId;
    private Long userId;
    private String mimeType;
    private Integer imageSizeBytes;
    private String storageProvider;
    private String storageUrl;
    private String storagePath;
    private String checksumSha256;
}
