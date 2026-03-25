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

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosetService {

    private final ClosetMapper closetMapper;
    private final S3UploadService s3UploadService;

    // 사용자 닉네임 조회
    public String getUserNickname(Long userId) {
        String nickname = closetMapper.findNicknameByUserId(userId);
        return nickname != null ? nickname : "회원" + userId;
    }

    // 사용자 프로필 사진 경로 조회
    public String getUserProfileImageUrl(Long userId) {
        return closetMapper.findProfileImageUrlByUserId(userId);
    }

    // 프로필 정보 수정 및 S3 연동
    @Transactional
    public void updateUserProfile(Long userId, String nickname, MultipartFile profileImage) {
        String imageUrl = closetMapper.findProfileImageUrlByUserId(userId);
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = s3UploadService.uploadImage(profileImage); 
        }
        closetMapper.updateUserProfile(userId, nickname, imageUrl);
    }

    // 회원 탈퇴 및 모든 활동 기록 물리 삭제 (참조 무결성 준수 순서)
    @Transactional
    public void withdrawUser(Long userId) {
        // 1. 코디 구성품 삭제 (최하위 자식)
        closetMapper.deleteAllRecItemsByUserId(userId);
        // 2. 코디 내역 삭제
        closetMapper.deleteAllRecommendationsByUserId(userId);
        // 3. 옷장이 지워지기 전 커스텀 상품 ID 목록 킵
        List<Long> productIds = closetMapper.findCustomProductIdsByUserId(userId);
        // 4. 옷장 데이터 삭제
        closetMapper.deleteAllClosetItemsByUserId(userId);
        // 5. 유저 전용 상품 정보 최종 삭제
        if (productIds != null && !productIds.isEmpty()) {
            closetMapper.deleteProductsByIds(productIds);
        }
        // 6. 최상위 부모 유저 삭제
        closetMapper.deleteUserById(userId);
    }

    // 옷장 데이터 조회
    public List<SavedCoordiDto> getSavedCoordis(Long userId) {
        return closetMapper.findSavedCoordis(userId);
    }

    public List<ClosetItemDto> getUserCloset(Long userId) {
        return closetMapper.findItemsByUserId(userId);
    }

    // 아이템 정보 및 이미지 수정 (파일이 있을 경우 S3 업로드 후 경로 교체)
    @Transactional
    public void updateClosetItem(ClosetItemDto dto, Long userId, MultipartFile imageFile) {
        Long productId = closetMapper.findProductIdByItemId(dto.getItemId(), userId);
        if (productId != null) {
            String imageUrl = dto.getImageUrl();
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = s3UploadService.uploadImage(imageFile);
            }

            ProductDto product = ProductDto.builder()
                    .productId(productId).name(dto.getName()).brand(dto.getBrand())
                    .color(dto.getColor()).season(dto.getSeason()).imageUrl(imageUrl).build();
            
            closetMapper.updateUserProduct(product);
        }
    }

    // 코디 제목 단독 수정
    @Transactional
    public void updateSavedCoordiTitle(Long recId, String newTitle, Long userId) {
        closetMapper.updateSavedCoordiTitle(recId, newTitle, userId);
    }

    // AI 추천 결과 저장
    @Transactional
    public void saveRecommendation(SavedCoordiDto dto) {
        if (dto.getInputMode() == null) dto.setInputMode("TEXT");
        if (dto.getProductOption() == null) dto.setProductOption("NONE");
        closetMapper.insertSavedCoordi(dto);
    }

    // 기존 추천 결과 상세 수정
    @Transactional
    public void updateRecommendation(SavedCoordiDto dto) {
        closetMapper.updateSavedCoordi(dto);
    }

    // 개별 옷 신규 등록 (다중 이미지)
    @Transactional
    public void addClosetItems(Long userId, ClosetItemDto itemDto, List<MultipartFile> imageFiles) {
        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty()) continue;
            String imageUrl = s3UploadService.uploadImage(imageFile);
            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM").categoryId(itemDto.getCategoryId()).name(itemDto.getName())
                    .brand(itemDto.getBrand()).color(itemDto.getColor()).season(itemDto.getSeason())
                    .imageUrl(imageUrl).build();
            closetMapper.insertUserProduct(newProduct);
            ClosetItemDto closetItem = new ClosetItemDto();
            closetItem.setUserId(userId); closetItem.setProductId(newProduct.getProductId());
            closetMapper.insertClosetItem(closetItem);
        }
    }

    // 수동 세트 등록 로직
    @Transactional
    public void addManualSet(Long userId, ManualSetDto setDto, List<MultipartFile> imageFiles) {
        SavedCoordiDto coordiDto = new SavedCoordiDto();
        coordiDto.setUserId(userId); coordiDto.setInputMode("MANUAL_SET"); coordiDto.setInputText(setDto.getSetName());
        coordiDto.setProductOption("NONE"); coordiDto.setTpoType("DAILY"); coordiDto.setStyleType("CASUAL");
        coordiDto.setAiExplanation(setDto.getSeason() + " 시즌 수동 등록 코디 세트"); coordiDto.setAiBlueprint("{}");
        
        closetMapper.insertSavedCoordi(coordiDto);
        Long recId = coordiDto.getRecId();

        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile file = imageFiles.get(i);
            if (file.isEmpty()) continue;
            String imageUrl = s3UploadService.uploadImage(file);
            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM").categoryId(setDto.getSetCategoryIds().get(i)).name(setDto.getSetItemNames().get(i))
                    .brand((setDto.getSetBrands() != null && setDto.getSetBrands().size() > i) ? setDto.getSetBrands().get(i) : null)
                    .color((setDto.getSetColors() != null && setDto.getSetColors().size() > i) ? setDto.getSetColors().get(i) : null)
                    .season(setDto.getSeason()).imageUrl(imageUrl).build();
            
            closetMapper.insertUserProduct(newProduct);
            ClosetItemDto closetItem = new ClosetItemDto();
            closetItem.setUserId(userId); closetItem.setProductId(newProduct.getProductId());
            closetMapper.insertClosetItem(closetItem);
            closetMapper.insertRecommendationItem(recId, "manual_slot_" + i, "CLOSET", closetItem.getItemId(), newProduct.getProductId());
        }
    }

    // 개별 아이템 삭제
    @Transactional
    public void removeClosetItem(Long itemId, Long userId) {
        Long productId = closetMapper.findProductIdByItemId(itemId, userId);
        if (productId != null) {
            closetMapper.deleteRecItemsByClosetItemId(itemId);
            closetMapper.deleteClosetItemById(itemId);
            closetMapper.deleteUserCustomProduct(productId);
        }
    }

    // 세트 삭제
    @Transactional
    public void deleteSavedCoordi(Long recId, Long userId) {
        String inputMode = closetMapper.findInputModeByRecId(recId);
        List<Long> closetItemIds = closetMapper.findClosetItemIdsByRecId(recId);
        closetMapper.deleteRecItemsByRecId(recId);
        closetMapper.deleteRecommendationById(recId, userId);
        if ("MANUAL_SET".equals(inputMode)) {
            for (Long itemId : closetItemIds) { if (itemId != null) removeClosetItem(itemId, userId); }
        }
    }
}