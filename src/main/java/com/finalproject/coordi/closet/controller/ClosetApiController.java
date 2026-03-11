package com.finalproject.coordi.closet.controller;

import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.service.ClosetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Long currentUserId = 1L; // TODO: 추후 Spring Security 유저 정보로 변경
        dto.setUserId(currentUserId);
        closetService.saveRecommendation(dto);
        
        return ResponseEntity.ok("코디가 옷장에 저장되었습니다.");
    }

    @PutMapping("/{recId}")
    public ResponseEntity<String> updateCoordi(@PathVariable Long recId, @RequestBody SavedCoordiDto dto) {
        Long currentUserId = 1L; // TODO: 추후 Spring Security 연동
        dto.setRecId(recId);
        dto.setUserId(currentUserId);
        closetService.updateRecommendation(dto);
        return ResponseEntity.ok("코디 정보가 수정되었습니다.");
    }

    @DeleteMapping("/{recId}")
    public ResponseEntity<String> deleteCoordi(@PathVariable Long recId) {
        Long currentUserId = 1L; // TODO: 추후 Spring Security 연동
        closetService.deleteSavedCoordi(recId, currentUserId);
        return ResponseEntity.ok("옷장에서 코디가 삭제되었습니다.");
    }
}