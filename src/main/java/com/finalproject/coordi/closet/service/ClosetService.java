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

    // 코디 추천 결과 수정
    public void updateRecommendation(SavedCoordiDto dto) {
        closetMapper.updateSavedCoordi(dto);
    }

    // 개별 옷장 아이템 다중 저장 로직 (상세 정보 포함)
    @SuppressWarnings("null")
    @Transactional
    public void addClosetItems(Long userId, ClosetItemDto itemDto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new RuntimeException("최소 1장 이상의 이미지 파일이 필요합니다.");
        }

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty()) continue;
            
            // S3 연동 전까지 더미 URL 사용 (에러 방지)
            String imageUrl = uploadDummyImage(imageFile);

            // 화면에서 입력받은 상세 정보를 ProductDto에 매핑
            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM")
                    .categoryId(itemDto.getCategoryId())
                    .name(itemDto.getName())
                    .brand(itemDto.getBrand())
                    .color(itemDto.getColor())
                    .material(itemDto.getMaterial())
                    .fit(itemDto.getFit())
                    .style(itemDto.getStyle())
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

    // 수동 코디 세트 저장 로직 (세트 구성품 상세 정보 포함)
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

            // 더미 URL 사용
            String imageUrl = uploadDummyImage(file);
            String itemName = setDto.getSetItemNames().get(i);
            Long categoryId = setDto.getSetCategoryIds().get(i);
            
            // 리스트에 데이터가 존재할 경우에만 꺼내옴 (IndexOutOfBoundsException 방지)
            String brand = (setDto.getSetBrands() != null && setDto.getSetBrands().size() > i) ? setDto.getSetBrands().get(i) : null;
            String color = (setDto.getSetColors() != null && setDto.getSetColors().size() > i) ? setDto.getSetColors().get(i) : null;
            String material = (setDto.getSetMaterials() != null && setDto.getSetMaterials().size() > i) ? setDto.getSetMaterials().get(i) : null;
            String fit = (setDto.getSetFits() != null && setDto.getSetFits().size() > i) ? setDto.getSetFits().get(i) : null;
            String style = (setDto.getSetStyles() != null && setDto.getSetStyles().size() > i) ? setDto.getSetStyles().get(i) : null;

            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM")
                    .categoryId(categoryId)
                    .name(itemName)
                    .brand(brand)
                    .color(color)
                    .material(material)
                    .fit(fit)
                    .style(style)
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

    // 개별 옷 완전 물리 삭제 (DB에서 아예 없앰)
    @Transactional
    public void removeClosetItem(Long itemId, Long userId) {
        Long productId = closetMapper.findProductIdByItemId(itemId, userId);
        
        if (productId != null) {
            // 자식 데이터부터 순서대로 지워야 DB 외래키(FK) 에러가 발생하지 않습니다.
            closetMapper.deleteRecItemsByClosetItemId(itemId); // 1. 코디에 묶인 고리 끊기
            closetMapper.deleteClosetItemById(itemId); // 2. 옷장에서 삭제
            closetMapper.deleteUserCustomProduct(productId); // 3. 실제 상품 데이터 파괴
        }
    }

    // 코디 세트 완전 물리 삭제 (DB에서 아예 없앰)
    @Transactional
    public void deleteSavedCoordi(Long recId, Long userId) {
        String inputMode = closetMapper.findInputModeByRecId(recId);
        List<Long> closetItemIds = closetMapper.findClosetItemIdsByRecId(recId);
        
        // 1. 코디 매핑(아이템 연결 고리) 일괄 삭제
        closetMapper.deleteRecItemsByRecId(recId);
        // 2. 코디 추천 뼈대 데이터 삭제
        closetMapper.deleteRecommendationById(recId, userId);
        
        // 3. 만약 수동으로 만든 코디 세트라면 세트와 함께 등록된 옷들까지 한꺼번에 싹 지워줍니다.
        if ("MANUAL_SET".equals(inputMode)) {
            for (Long itemId : closetItemIds) {
                if (itemId != null) {
                    removeClosetItem(itemId, userId);
                }
            }
        }
    }

    // 임시 더미 이미지 URL 생성 (로컬 테스트 전용)
    private String uploadDummyImage(MultipartFile file) {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        return "https://final-s3-demo-bucket.s3.ap-northeast-2.amazonaws.com/closet/" + fileName;
    }
}