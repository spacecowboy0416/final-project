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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosetService {

    private final ClosetMapper closetMapper;
    private final S3UploadService s3UploadService;

    // 사용자 닉네임 조회 기능
    public String getUserNickname(Long userId) {
        String nickname = closetMapper.findNicknameByUserId(userId);
        return nickname != null ? nickname : "회원" + userId;
    }

    // 사용자 프로필 사진 경로 조회 기능
    public String getUserProfileImageUrl(Long userId) {
        return closetMapper.findProfileImageUrlByUserId(userId);
    }

    // 내가 작성한 게시글 및 페이징 정보 조회 기능 (모달 내부 표시용)
    public Map<String, Object> getMyBoardData(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Map<String, Object>> posts = closetMapper.findMyPosts(userId, offset, size);
        int totalPosts = closetMapper.countMyPosts(userId);
        int totalPages = (int) Math.ceil((double) totalPosts / size);

        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        return result;
    }

    // 사용자 프로필 정보 수정 제어 로직
    @Transactional
    public void updateUserProfile(Long userId, String nickname, MultipartFile profileImage) {
        String imageUrl = closetMapper.findProfileImageUrlByUserId(userId);
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = s3UploadService.uploadImage(profileImage); 
        }
        closetMapper.updateUserProfile(userId, nickname, imageUrl);
    }

    // 회원 탈퇴 및 모든 활동 기록 물리 삭제 (참조 무결성 준수 역순 삭제)
    @Transactional
    public void withdrawUser(Long userId) {
        // 1. 댓글 및 게시글 삭제 (외래키 참조 관계의 최하단)
        closetMapper.deleteAllCommentsByUserId(userId);
        closetMapper.deleteAllPostsByUserId(userId);
        
        // 2. 코디 구성품 삭제
        closetMapper.deleteAllRecItemsByUserId(userId);
        // 3. 코디 내역 삭제
        closetMapper.deleteAllRecommendationsByUserId(userId);
        // 4. 옷장이 지워지기 전 커스텀 상품 ID 목록 킵
        List<Long> productIds = closetMapper.findCustomProductIdsByUserId(userId);
        // 5. 옷장 데이터 삭제
        closetMapper.deleteAllClosetItemsByUserId(userId);
        // 6. 유저 전용 상품 정보 최종 삭제
        if (productIds != null && !productIds.isEmpty()) {
            closetMapper.deleteProductsByIds(productIds);
        }
        // 7. 최상위 부모 유저 삭제
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

    // 개별 아이템 정보 및 이미지 수정 제어 로직
    @Transactional
    public void updateClosetItem(ClosetItemDto dto, Long userId, MultipartFile imageFile) {
        Long productId = closetMapper.findProductIdByItemId(dto.getItemId(), userId);
        if (productId != null) {
            String finalImageUrl = closetMapper.findProductImageUrlById(productId);
            
            if (imageFile != null && !imageFile.isEmpty()) {
                finalImageUrl = s3UploadService.uploadImage(imageFile);
            }

            ProductDto product = ProductDto.builder()
                    .productId(productId)
                    .name(dto.getName())
                    .brand(dto.getBrand())
                    .color(dto.getColor())
                    .season(dto.getSeason())
                    .imageUrl(finalImageUrl)
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
        if (dto.getInputMode() == null) dto.setInputMode("TEXT");
        if (dto.getProductOption() == null) dto.setProductOption("NONE");
        closetMapper.insertSavedCoordi(dto);
    }

    // 기존 추천 결과 상세 수정
    @Transactional
    public void updateRecommendation(SavedCoordiDto dto) {
        closetMapper.updateSavedCoordi(dto);
    }

    // 다중 이미지 기반 개별 아이템 등록 제어 로직
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

    // 수동 조합 세트 및 구성 아이템 일괄 등록 제어 로직
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

    // 옷장 개별 아이템 및 연관 상품 물리 삭제 제어 로직
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

        closetMapper.deleteBoardCommentsByRecId(recId);
        closetMapper.deleteBoardPostsByRecId(recId);
        closetMapper.deleteRecItemsByRecId(recId);
        closetMapper.deleteRecommendationById(recId, userId);
        
        if ("MANUAL_SET".equals(inputMode)) {
            for (Long itemId : closetItemIds) { 
                if (itemId != null) removeClosetItem(itemId, userId); 
            }
        }
    }
}