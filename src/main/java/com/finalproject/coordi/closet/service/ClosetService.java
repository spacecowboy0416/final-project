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

    // 사용자 닉네임을 데이터베이스에서 조회합니다. 값이 없을 경우 기본 식별자를 반환
    public String getUserNickname(Long userId) {
        String nickname = closetMapper.findNicknameByUserId(userId);
        return nickname != null ? nickname : "회원" + userId;
    }

    // 데이터베이스에 저장된 사용자의 프로필 이미지 URL을 반환
    public String getUserProfileImageUrl(Long userId) {
        return closetMapper.findProfileImageUrlByUserId(userId);
    }

    // 사용자의 닉네임과 프로필 이미지를 업데이트
    @Transactional
    public void updateUserProfile(Long userId, String nickname, MultipartFile profileImage) {
        String imageUrl = closetMapper.findProfileImageUrlByUserId(userId);
        // 클라우드(S3) 인프라 연동 전이므로, 이미지 수정 시 로컬 테스트용 더미 URL을 생성
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = uploadDummyImage(profileImage); 
        }
        closetMapper.updateUserProfile(userId, nickname, imageUrl);
    }

    // 사용자 식별자를 기반으로 회원 정보를 데이터베이스에서 삭제
    @Transactional
    public void withdrawUser(Long userId) {
        closetMapper.deleteUserById(userId);
    }

    // 특정 사용자가 옷장에 저장한 전체 코디 목록을 조회
    public List<SavedCoordiDto> getSavedCoordis(Long userId) {
        return closetMapper.findSavedCoordis(userId);
    }

    // 특정 사용자의 옷장에 등록된 개별 의류 아이템 목록을 조회
    public List<ClosetItemDto> getUserCloset(Long userId) {
        return closetMapper.findItemsByUserId(userId);
    }

    // AI 코디 추천 엔진을 통해 도출된 결과 데이터를 옷장에 저장
    public void saveRecommendation(SavedCoordiDto dto) {
        try {
            if (dto.getInputMode() == null) dto.setInputMode("TEXT");
            if (dto.getProductOption() == null) dto.setProductOption("NONE");
            closetMapper.insertSavedCoordi(dto);
        } catch (Exception e) {
            throw new RuntimeException("코디 추천 결과를 저장하는 중 문제가 발생했습니다.", e);
        }
    }

    // 기존에 저장된 코디 세트의 상세 정보를 수정
    public void updateRecommendation(SavedCoordiDto dto) {
        closetMapper.updateSavedCoordi(dto);
    }

    // 업로드된 다수의 의류 이미지를 개별 상품 엔티티로 생성하고 옷장 아이템으로 매핑하여 저장
    @SuppressWarnings("null")
    @Transactional
    public void addClosetItems(Long userId, ClosetItemDto itemDto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new RuntimeException("최소 1장 이상의 이미지 파일이 필요합니다.");
        }

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty()) continue;
            
            // S3 연동 이전 단계이므로 UI 테스트를 위한 더미 이미지 URL을 생성
            String imageUrl = uploadDummyImage(imageFile);

            // 전달받은 상세 속성을 반영하여 새로운 Product 엔티티 객체를 빌드
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
            
            // 등록된 Product 데이터를 기반으로 사용자의 옷장과 연결하는 레코드를 생성
            ClosetItemDto closetItem = new ClosetItemDto();
            closetItem.setUserId(userId);
            closetItem.setProductId(newProduct.getProductId());
            closetMapper.insertClosetItem(closetItem);
        }
    }

    // 사용자가 여러 의류 이미지를 직접 조합하여 구성한 수동 코디 세트를 생성
    @Transactional
    public void addManualSet(Long userId, ManualSetDto setDto, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new RuntimeException("최소 1장 이상의 이미지 파일이 필요합니다.");
        }

        // 1. 코디 세트의 기본 메타데이터(부모 레코드)를 먼저 생성
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

        // 2. 세트를 구성하는 각 의류 이미지를 순회하며 개별 상품으로 등록 후 코디 그룹에 연결
        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile file = imageFiles.get(i);
            if (file.isEmpty()) continue;

            String imageUrl = uploadDummyImage(file);
            String itemName = setDto.getSetItemNames().get(i);
            Long categoryId = setDto.getSetCategoryIds().get(i);
            
            // 리스트 파라미터의 인덱스 참조 시 NullPointerException을 방지하기 위한 안전 검사
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

            // 신규 생성된 개별 아이템을 부모 레코드인 코디 세트에 매핑
            closetMapper.insertRecommendationItem(recId, "manual_slot_" + i, "CLOSET", closetItem.getItemId(), newProduct.getProductId());
        }
    }

    // 지정된 옷장 아이템과 연관된 모든 하위 데이터를 포함하여 물리적으로 완전 삭제
    @Transactional
    public void removeClosetItem(Long itemId, Long userId) {
        Long productId = closetMapper.findProductIdByItemId(itemId, userId);
        
        if (productId != null) {
            // 참조 무결성(Foreign Key) 오류를 방지하기 위해 자식 레코드부터 역순으로 삭제
            closetMapper.deleteRecItemsByClosetItemId(itemId); 
            closetMapper.deleteClosetItemById(itemId); 
            closetMapper.deleteUserCustomProduct(productId); 
        }
    }

    // 지정된 코디 세트를 완전 삭제하며, 수동 세트일 경우 포함된 개별 아이템까지 일괄 파기
    @Transactional
    public void deleteSavedCoordi(Long recId, Long userId) {
        String inputMode = closetMapper.findInputModeByRecId(recId);
        List<Long> closetItemIds = closetMapper.findClosetItemIdsByRecId(recId);
        
        // 1. 코디 세트와 아이템 간의 다대다 매핑 관계 데이터를 일괄 삭제
        closetMapper.deleteRecItemsByRecId(recId);
        // 2. 코디 그룹 자체의 메타데이터를 삭제합니다.
        closetMapper.deleteRecommendationById(recId, userId);
        
        // 3. 해당 코디가 사용자에 의해 수동으로 일괄 등록된 세트라면, 종속된 개별 옷장 아이템도 함께 정리
        if ("MANUAL_SET".equals(inputMode)) {
            for (Long itemId : closetItemIds) {
                if (itemId != null) {
                    removeClosetItem(itemId, userId);
                }
            }
        }
    }

    // AWS 인프라가 적용되지 않은 개발 환경에서 프론트엔드 연동을 위해 임시 객체 URL을 생성
    private String uploadDummyImage(MultipartFile file) {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        return "https://final-s3-demo-bucket.s3.ap-northeast-2.amazonaws.com/closet/" + fileName;
    }
}