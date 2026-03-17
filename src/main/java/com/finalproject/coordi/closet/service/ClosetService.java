package com.finalproject.coordi.closet.service;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.ManualSetDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.mapper.ClosetMapper;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosetService {

    private final ClosetMapper closetMapper;

    // 저장된 전체 코디 목록 반환
    public List<SavedCoordiDto> getSavedCoordis(Long userId) {
        return closetMapper.findSavedCoordis(userId);
    }

    // 개별 옷장 아이템 목록 반환
    public List<ClosetItemDto> getUserCloset(Long userId) {
        return closetMapper.findItemsByUserId(userId);
    }

    // AI 코디 추천 결과 저장
    public void saveRecommendation(SavedCoordiDto dto) {
        try {
            if (dto.getInputMode() == null) dto.setInputMode("TEXT");
            if (dto.getProductOption() == null) dto.setProductOption("NONE");
            closetMapper.insertSavedCoordi(dto);
        } catch (Exception e) {
            throw new RuntimeException("코디 추천 결과를 저장하는 중 문제가 발생했습니다.", e);
        }
    }

    public void updateRecommendation(SavedCoordiDto dto) {
        closetMapper.updateSavedCoordi(dto);
    }

    public void deleteSavedCoordi(Long recId, Long userId) {
        closetMapper.unsaveCoordi(recId, userId);
    }

    // 개별 옷장 아이템 다중 저장 로직
    @SuppressWarnings("null")
    @Transactional
    public void addClosetItems(Long userId, ClosetItemDto itemDto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new RuntimeException("최소 1장 이상의 이미지 파일이 필요합니다.");
        }

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty()) continue;
            String imageUrl = uploadImageToS3(imageFile);

            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM")
                    .categoryId(itemDto.getCategoryId())
                    .name(itemDto.getName())
                    .season(itemDto.getSeason())
                    .imageUrl(imageUrl)
                    .build();

            closetMapper.insertUserProduct(newProduct);
            
            ClosetItemDto closetItem = new ClosetItemDto();
            closetItem.setUserId(userId);
            closetItem.setProductId(newProduct.getProductId());
            closetMapper.insertClosetItem(closetItem);
        }
    }

    // 수동 코디 세트 저장 로직
    @Transactional
    public void addManualSet(Long userId, ManualSetDto setDto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new RuntimeException("최소 1장 이상의 이미지 파일이 필요합니다.");
        }

        // 1. 코디 세트 뼈대 생성 및 저장
        SavedCoordiDto coordiDto = new SavedCoordiDto();
        coordiDto.setUserId(userId);
        coordiDto.setInputMode("MANUAL_SET");
        coordiDto.setInputText(setDto.getSetName());
        coordiDto.setProductOption("NONE");
        coordiDto.setTpoType("DAILY");
        coordiDto.setStyleType("CASUAL");
        coordiDto.setAiExplanation(setDto.getSeason() + " 시즌 수동 등록 코디 세트");
        coordiDto.setAiBlueprint("{}");
        
        closetMapper.insertSavedCoordi(coordiDto);
        Long recId = coordiDto.getRecId();

        // 2. 세트에 포함된 개별 아이템 정보 저장 및 매핑
        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile file = imageFiles.get(i);
            if (file.isEmpty()) continue;

            String imageUrl = uploadImageToS3(file);
            String itemName = setDto.getSetItemNames().get(i);
            Long categoryId = setDto.getSetCategoryIds().get(i);

            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM")
                    .categoryId(categoryId)
                    .name(itemName)
                    .season(setDto.getSeason())
                    .imageUrl(imageUrl)
                    .build();
            
            closetMapper.insertUserProduct(newProduct);

            ClosetItemDto closetItem = new ClosetItemDto();
            closetItem.setUserId(userId);
            closetItem.setProductId(newProduct.getProductId());
            closetMapper.insertClosetItem(closetItem);

            closetMapper.insertRecommendationItem(recId, "manual_slot_" + i, "CLOSET", closetItem.getItemId(), newProduct.getProductId());
        }
    }

    public void removeClosetItem(Long itemId, Long userId) {
        closetMapper.deleteItem(itemId, userId);
    }

    // 이미지 S3 업로드 로직 (현재는 더미 URL 반환 구조)
    private String uploadImageToS3(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            return "https://final-s3-demo-bucket.s3.ap-northeast-2.amazonaws.com/closet/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("이미지 파일 업로드에 실패했습니다.", e);
        }
    }
}