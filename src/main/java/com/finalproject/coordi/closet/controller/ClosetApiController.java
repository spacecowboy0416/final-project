package com.finalproject.coordi.closet.controller;

import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.service.ClosetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/closet/recommendations")
@RequiredArgsConstructor
public class ClosetApiController {

    private final ClosetService closetService;

    @PostMapping
    public ResponseEntity<String> saveCoordi(@RequestBody SavedCoordiDto dto) {
        try {
            Long currentUserId = 1L; 
            dto.setUserId(currentUserId);
            closetService.saveRecommendation(dto);
            return ResponseEntity.ok("코디가 옷장에 저장되었습니다.");
        } catch (Exception e) {
            log.error("API: 코디 저장 실패", e);
            // 에러 발생 시 프론트엔드 쪽에 500 에러 상태코드와 메시지를 전달합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("코디 저장 중 서버 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{recId}")
    public ResponseEntity<String> updateCoordi(@PathVariable Long recId, @RequestBody SavedCoordiDto dto) {
        Long currentUserId = 1L;
        dto.setRecId(recId);
        dto.setUserId(currentUserId);
        closetService.updateRecommendation(dto);
        return ResponseEntity.ok("코디 정보가 수정되었습니다.");
    }

    @DeleteMapping("/{recId}")
    public ResponseEntity<String> deleteCoordi(@PathVariable Long recId) {
        Long currentUserId = 1L;
        closetService.deleteSavedCoordi(recId, currentUserId);
        return ResponseEntity.ok("옷장에서 코디가 삭제되었습니다.");
    }
}