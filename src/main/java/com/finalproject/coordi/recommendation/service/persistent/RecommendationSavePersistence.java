package com.finalproject.coordi.recommendation.service.persistent;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.recommendation.domain.enums.CodedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.ProductEnums.ProductCategoryCode;
import com.finalproject.coordi.recommendation.dto.api.CoordinationItemOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RecommendationSaveRequestDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductImageMetadataDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationDto;
import com.finalproject.coordi.recommendation.dto.persistent.RecommendationItemDto;
import com.finalproject.coordi.recommendation.mapper.RecommendationMapper;
import com.finalproject.coordi.recommendation.service.productSearch.ImageUploadPort;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 저장 요청을 recommendation 관련 테이블에 영속화한다.
 */
@Repository
@RequiredArgsConstructor
public class RecommendationSavePersistence {
    private static final String INPUT_MODE_TEXT = "TEXT";
    private static final String PRODUCT_OPTION_SEARCH_TOP1 = "SEARCH_TOP1";
    private static final String SOURCE_TYPE_PRODUCT = "PRODUCT";
    private static final String SOURCE_TYPE_MAIN_ITEM = "MAIN_ITEM";
    private static final String DEFAULT_PRODUCT_SOURCE = "NAVER";
    private static final String MAIN_ITEM_PRODUCT_SOURCE = "MAIN_UPLOAD";
    private static final String DEFAULT_MAIN_ITEM_NAME = "main_item";
    private static final String EMPTY_JSON = "{}";
    private static final String DEFAULT_MAIN_ITEM_IMAGE_MIME_TYPE = "image/jpeg";
    private static final String STORAGE_PATH_MAIN_ITEM = "recommendations/main-item";

    private final RecommendationMapper recommendationMapper;
    private final ImageUploadPort imageUploadPort;

    @Transactional
    public Long save(Long userId, RecommendationSaveRequestDto request) {
        RecommendationDto recommendationDto = RecommendationDto.builder()
            .userId(userId)
            .weatherStatus(request.weatherStatusCode())
            .inputMode(INPUT_MODE_TEXT)
            .inputText(request.naturalText())
            .productOption(PRODUCT_OPTION_SEARCH_TOP1)
            .tpoType(request.tpoType().getCode())
            .styleType(request.styleType().getCode())
            .isSaved(true)
            .aiBlueprint(EMPTY_JSON)
            .aiExplanation(request.aiExplanationOrEmpty())
            .build();
        recommendationMapper.insertRecommendation(recommendationDto);

        Long recId = recommendationDto.getRecId();
        if (recId == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }

        MainItemImageContext mainItemImageContext = resolveMainItemImageContext(request);

        for (CoordinationItemOutputDto coordinationItem : request.persistableCoordination()) {
            Long productId = upsertProduct(userId, coordinationItem, mainItemImageContext);
            if (productId == null) {
                continue;
            }

            Long closetItemId = coordinationItem.isMyItem()
                ? ensureClosetItem(userId, productId)
                : null;

            recommendationMapper.insertRecommendationItem(RecommendationItemDto.builder()
                .recId(recId)
                .slotKey(coordinationItem.slotKey().getCode())
                .sourceType(coordinationItem.isMyItem() ? SOURCE_TYPE_MAIN_ITEM : SOURCE_TYPE_PRODUCT)
                .closetItemId(closetItemId)
                .productId(productId)
                .searchQuery(request.searchQueryOf(coordinationItem.slotKey()))
                .priority(coordinationItem.priority() == null ? null : coordinationItem.priority().getCode())
                .matchScore(coordinationItem.matchScore())
                .reason(coordinationItem.reasoning())
                .build());
        }

        return recId;
    }

    private Long upsertProduct(
        Long userId,
        CoordinationItemOutputDto coordinationItem,
        MainItemImageContext mainItemImageContext
    ) {
        Long categoryId = resolveCategoryId(coordinationItem.slotKey());
        if (categoryId == null) {
            return null;
        }

        ProductDto productDto;
        if (coordinationItem.isMyItem()) {
            if (mainItemImageContext == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            Long existingProductId = recommendationMapper.findProductIdByChecksum(mainItemImageContext.checksumSha256());
            if (existingProductId != null) {
                String existingImageUrl = recommendationMapper.findProductImageUrlByProductId(existingProductId);
                if (hasText(existingImageUrl)) {
                    return existingProductId;
                }

                ImageUploadPort.UploadedImage uploadedImage = uploadMainItemImage(mainItemImageContext);
                recommendationMapper.updateProductImageUrlById(existingProductId, uploadedImage.storageUrl());
                insertMainItemImageMetadata(userId, existingProductId, uploadedImage, mainItemImageContext.checksumSha256());
                return existingProductId;
            }

            ImageUploadPort.UploadedImage uploadedImage = uploadMainItemImage(mainItemImageContext);
            productDto = ProductDto.builder()
                .source(MAIN_ITEM_PRODUCT_SOURCE)
                .externalId(UUID.randomUUID().toString())
                .categoryId(categoryId)
                .name(resolveMainItemName(coordinationItem.itemName()))
                .imageUrl(uploadedImage.storageUrl())
                .color(toCode(coordinationItem.color()))
                .material(toCode(coordinationItem.material()))
                .fit(toCode(coordinationItem.fit()))
                .style(toCode(coordinationItem.style()))
                .tempMin(coordinationItem.tempMin())
                .tempMax(coordinationItem.tempMax())
                .build();
            recommendationMapper.upsertProduct(productDto);
            Long productId = productDto.getProductId();
            if (productId == null) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR);
            }
            insertMainItemImageMetadata(userId, productId, uploadedImage, mainItemImageContext.checksumSha256());
            return productId;
        } else {
            if (coordinationItem.marketplaceProductId() == null || coordinationItem.marketplaceProductId().isBlank()) {
                return null;
            }
            productDto = ProductDto.builder()
                .source(resolveProductSource(coordinationItem.marketplaceProvider()))
                .externalId(coordinationItem.marketplaceProductId())
                .categoryId(categoryId)
                .name(coordinationItem.itemName())
                .brand(coordinationItem.brandName())
                .price(coordinationItem.salePrice())
                .imageUrl(coordinationItem.imageUrl())
                .link(coordinationItem.productDetailUrl())
                .color(toCode(coordinationItem.color()))
                .material(toCode(coordinationItem.material()))
                .fit(toCode(coordinationItem.fit()))
                .style(toCode(coordinationItem.style()))
                .tempMin(coordinationItem.tempMin())
                .tempMax(coordinationItem.tempMax())
                .build();
            recommendationMapper.upsertProduct(productDto);
            return productDto.getProductId();
        }
    }

    private Long resolveCategoryId(CategoryType slotKey) {
        try {
            return recommendationMapper.findCategoryIdByCode(ProductCategoryCode.fromSlotType(slotKey));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String toCode(CodedEnum codedEnum) {
        return codedEnum == null ? null : codedEnum.getCode();
    }

    private String resolveMainItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return DEFAULT_MAIN_ITEM_NAME;
        }
        return itemName;
    }

    private String resolveProductSource(String marketplaceProvider) {
        if (marketplaceProvider == null || marketplaceProvider.isBlank()) {
            return DEFAULT_PRODUCT_SOURCE;
        }
        return marketplaceProvider;
    }

    private MainItemImageContext resolveMainItemImageContext(RecommendationSaveRequestDto request) {
        if (request == null || !request.hasMainItem()) {
            return null;
        }
        if (request.mainItemImageBase64() == null || request.mainItemImageBase64().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(request.mainItemImageBase64());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, exception);
        }
        if (imageBytes.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String checksum = calculateSha256(imageBytes);
        return new MainItemImageContext(
            imageBytes,
            resolveMainItemMimeType(request.mainItemImageMimeType()),
            checksum
        );
    }

    private String calculateSha256(byte[] imageBytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(imageBytes);
            StringBuilder hexBuilder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, exception);
        }
    }

    private Long ensureClosetItem(Long userId, Long productId) {
        Long closetItemId = recommendationMapper.findClosetItemIdByUserAndProduct(userId, productId);
        if (closetItemId != null) {
            return closetItemId;
        }
        recommendationMapper.insertClosetItem(userId, productId);
        Long insertedClosetItemId = recommendationMapper.findClosetItemIdByUserAndProduct(userId, productId);
        if (insertedClosetItemId == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
        return insertedClosetItemId;
    }

    private String resolveMainItemMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return DEFAULT_MAIN_ITEM_IMAGE_MIME_TYPE;
        }
        return mimeType;
    }

    private ImageUploadPort.UploadedImage uploadMainItemImage(MainItemImageContext mainItemImageContext) {
        // 추천 저장 경로에서 main item 이미지는 recommendation prefix로 업로드한다.
        return imageUploadPort.uploadImage(
            new ImageUploadPort.ImageUploadRequest(
                mainItemImageContext.imageBytes(),
                mainItemImageContext.mimeType(),
                STORAGE_PATH_MAIN_ITEM
            )
        );
    }

    private void insertMainItemImageMetadata(
        Long userId,
        Long productId,
        ImageUploadPort.UploadedImage uploadedImage,
        String checksumSha256
    ) {
        recommendationMapper.insertProductImageMetadata(ProductImageMetadataDto.builder()
            .productId(productId)
            .userId(userId)
            .mimeType(uploadedImage.mimeType())
            .imageSizeBytes(uploadedImage.imageSizeBytes())
            .storageProvider(uploadedImage.storageProvider())
            .storageUrl(uploadedImage.storageUrl())
            .storagePath(uploadedImage.storagePath())
            .checksumSha256(checksumSha256)
            .build());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record MainItemImageContext(
        byte[] imageBytes,
        String mimeType,
        String checksumSha256
    ) {
    }
}
