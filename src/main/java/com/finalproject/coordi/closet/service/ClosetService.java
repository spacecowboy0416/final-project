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

// 옷장 도메인 핵심 비즈니스 로직 처리 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class ClosetService {

    private final ClosetMapper closetMapper;
    private final S3UploadService s3UploadService; // 실제 S3 업로드 서비스 의존성 주입

    // 사용자 닉네임 조회 기능
    public String getUserNickname(Long userId) {
        String nickname = closetMapper.findNicknameByUserId(userId);
        return nickname != null ? nickname : "회원" + userId;
    }

    // 사용자 프로필 이미지 경로 조회 기능
    public String getUserProfileImageUrl(Long userId) {
        return closetMapper.findProfileImageUrlByUserId(userId);
    }

    // 사용자 프로필 정보 수정 제어 로직 (실제 S3 업로드 연동)
    @Transactional
    public void updateUserProfile(Long userId, String nickname, MultipartFile profileImage) {
        String imageUrl = closetMapper.findProfileImageUrlByUserId(userId);
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = s3UploadService.uploadImage(profileImage); 
        }
        closetMapper.updateUserProfile(userId, nickname, imageUrl);
    }

    // 회원 탈퇴 처리 기능
    @Transactional
    public void withdrawUser(Long userId) {
        closetMapper.deleteUserById(userId);
    }

    // 저장된 전체 코디 내역 조회 기능
    public List<SavedCoordiDto> getSavedCoordis(Long userId) {
        return closetMapper.findSavedCoordis(userId);
    }

    // 옷장 개별 아이템 리스트 조회 기능
    public List<ClosetItemDto> getUserCloset(Long userId) {
        return closetMapper.findItemsByUserId(userId);
    }

    // 개별 아이템 수정 및 세트 데이터 자동 동기화 기능
    @Transactional
    public void updateClosetItem(ClosetItemDto dto, Long userId) {
        Long productId = closetMapper.findProductIdByItemId(dto.getItemId(), userId);
        if (productId != null) {
            ProductDto product = ProductDto.builder()
                    .productId(productId)
                    .name(dto.getName())
                    .brand(dto.getBrand())
                    .color(dto.getColor())
                    .season(dto.getSeason())
                    .build();
            closetMapper.updateUserProduct(product);
        }
    }

    // 코디 세트 제목 수정 제어 로직
    @Transactional
    public void updateSavedCoordiTitle(Long recId, String newTitle, Long userId) {
        closetMapper.updateSavedCoordiTitle(recId, newTitle, userId);
    }

    // AI 코디 추천 결과 데이터 영구 저장 제어 로직
    @Transactional
    public void saveRecommendation(SavedCoordiDto dto) {
        try {
            if (dto.getInputMode() == null) dto.setInputMode("TEXT");
            if (dto.getProductOption() == null) dto.setProductOption("NONE");
            closetMapper.insertSavedCoordi(dto);
        } catch (Exception e) {
            throw new RuntimeException("코디 추천 결과를 저장하는 중 문제가 발생했습니다.", e);
        }
    }

    // 기존 저장된 코디 추천 결과의 상세 정보 수정 제어 로직
    @Transactional
    public void updateRecommendation(SavedCoordiDto dto) {
        try {
            closetMapper.updateSavedCoordi(dto);
        } catch (Exception e) {
            throw new RuntimeException("코디 정보를 수정하는 중 문제가 발생했습니다.", e);
        }
    }

    // 다중 이미지 기반 개별 아이템 등록 제어 로직 (실제 S3 업로드 연동 완료)
    @Transactional
    public void addClosetItems(Long userId, ClosetItemDto itemDto, List<MultipartFile> imageFiles) {
        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty()) continue;
            
            // 더미 URL 대신 실제 S3 버킷에 업로드 후 반환된 진짜 URL 획득
            String imageUrl = s3UploadService.uploadImage(imageFile);

            ProductDto newProduct = ProductDto.builder()
                    .source("USER_CUSTOM").categoryId(itemDto.getCategoryId()).name(itemDto.getName())
                    .brand(itemDto.getBrand()).color(itemDto.getColor()).season(itemDto.getSeason())
                    .imageUrl(imageUrl).build();

            closetMapper.insertUserProduct(newProduct);
            
            ClosetItemDto closetItem = new ClosetItemDto();
            closetItem.setUserId(userId);
            closetItem.setProductId(newProduct.getProductId());
            closetMapper.insertClosetItem(closetItem);
        }
    }

    // 수동 조합 세트 및 구성 아이템 일괄 등록 제어 로직 (실제 S3 업로드 연동 완료)
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
            
            // 더미 URL 대신 실제 S3 버킷에 업로드 후 반환된 진짜 URL 획득
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

    // 옷장 아이템 및 연관 상품 물리 삭제 제어 로직
    @Transactional
    public void removeClosetItem(Long itemId, Long userId) {
        Long productId = closetMapper.findProductIdByItemId(itemId, userId);
        if (productId != null) {
            closetMapper.deleteRecItemsByClosetItemId(itemId);
            closetMapper.deleteClosetItemById(itemId);
            closetMapper.deleteUserCustomProduct(productId);
        }
    }

    // 코디 정보 및 수동 세트 연관 아이템 일괄 삭제 제어 로직
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