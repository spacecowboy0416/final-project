package com.finalproject.coordi.closet.controller;

import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.service.ClosetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@Slf4j
@RestController
@RequestMapping("/api/closet/recommendations")
@RequiredArgsConstructor
public class ClosetApiController {

    private final ClosetService closetService;

    // SecurityContext에서 현재 요청 객체의 사용자 인증 정보를 검증하고 추출
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        User user = (User) authentication.getPrincipal();
        return Long.parseLong(user.getUsername()); 
    }

    // 클라이언트로부터 전달받은 AI 코디 추천 데이터를 데이터베이스에 영구 저장
    @PostMapping
    public ResponseEntity<String> saveCoordi(@RequestBody SavedCoordiDto dto) {
        dto.setUserId(getCurrentUserId()); 
        closetService.saveRecommendation(dto); 
        return ResponseEntity.ok("코디가 옷장에 저장되었습니다.");
    }

    // 옷장에 이미 저장된 특정 코디 세트의 상세 정보를 수정
    @PutMapping("/{recId}")
    public ResponseEntity<String> updateCoordi(@PathVariable("recId") Long recId, @RequestBody SavedCoordiDto dto) {
        dto.setRecId(recId); 
        dto.setUserId(getCurrentUserId()); 
        closetService.updateRecommendation(dto); 
        return ResponseEntity.ok("코디 정보가 수정되었습니다.");
    }

    // 지정된 식별자(PK)를 바탕으로 사용자의 옷장에서 특정 코디를 삭제
    @DeleteMapping("/{recId}")
    public ResponseEntity<String> deleteCoordi(@PathVariable("recId") Long recId) {
        closetService.deleteSavedCoordi(recId, getCurrentUserId()); 
        return ResponseEntity.ok("옷장에서 코디가 삭제되었습니다.");
    }
}