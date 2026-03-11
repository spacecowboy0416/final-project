package com.finalproject.coordi.closet.service;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.mapper.ClosetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosetService {

    private final ClosetMapper closetMapper;

    // 코디 추천 결과 관리
    public List<SavedCoordiDto> getSavedCoordis(Long userId) {
        return closetMapper.findSavedCoordis(userId);
    }

    public void saveRecommendation(SavedCoordiDto dto) {
        try {
            if (dto.getInputMode() == null) dto.setInputMode("TEXT");
            if (dto.getProductOption() == null) dto.setProductOption("NONE");
            closetMapper.insertSavedCoordi(dto);
            log.info("코디 저장 성공 - userId: {}", dto.getUserId());
        } catch (Exception e) {
            log.error("코디 추천 결과 저장 중 DB 에러 발생 - userId: {}", dto.getUserId(), e);
            // 에러를 던져 GlobalExceptionHandler가 낚아채서 AI 에러 로그 DB에 저장
            throw new RuntimeException("코디 추천 결과를 저장하는 중 문제가 발생했습니다.", e);
        }
    }

    public void updateRecommendation(SavedCoordiDto dto) {
        closetMapper.updateSavedCoordi(dto);
    }

    public void deleteSavedCoordi(Long recId, Long userId) {
        closetMapper.unsaveCoordi(recId, userId);
    }

    // 개별 옷 아이템 관리
    public List<ClosetItemDto> getUserCloset(Long userId) {
        return closetMapper.findItemsByUserId(userId);
    }

    public void addClosetItem(Long userId, ClosetItemDto itemDto, MultipartFile imageFile) {
        String imageUrl = null;
        
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = uploadImageToS3(imageFile); 
            }
        } catch (Exception e) {
            log.error("옷장 이미지 S3 업로드 실패 - 파일명: {}", imageFile.getOriginalFilename(), e);
            throw new RuntimeException("이미지 파일 업로드에 실패했습니다. 파일 용량이나 형식을 확인해주세요.", e);
        }

        // DB 저장 처리
        try {
            itemDto.setUserId(userId);
            itemDto.setImageUrl(imageUrl);
            closetMapper.insertItem(itemDto);
            log.info("나의 옷장 아이템 등록 성공 - userId: {}, itemId: {}", userId, itemDto.getItemId());
        } catch (Exception e) {
            log.error("옷장 아이템 DB 저장 실패 - userId: {}", userId, e);
            throw new RuntimeException("옷 정보를 데이터베이스에 저장하지 못했습니다.", e);
        }
    }

    public void removeClosetItem(Long itemId, Long userId) {
        closetMapper.deleteItem(itemId, userId);
    }

    // 임시 S3 업로드 메서드
    private String uploadImageToS3(MultipartFile file) {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        return "https://final-s3-demo-bucket.s3.ap-northeast-2.amazonaws.com/closet/" + fileName;
    }
}