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

    private Long getCurrentUserId() {
        // 1. Spring Security 컨텍스트에서 현재 요청의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 없거나 익명 사용자일 경우 접근 차단 (예외 발생)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        // 3. JwtProvider에서 토큰 검증 후 SecurityContext에 저장해둔 User 객체 추출
        User user = (User) authentication.getPrincipal();
        
        // 4. JwtProvider 설정 상 username 위치에 userId가 저장되어 있으므로 추출 후 Long 타입으로 변환하여 반환
        return Long.parseLong(user.getUsername()); 
    }

    // AI 코디 추천 결과를 옷장에 저장
    @PostMapping
    public ResponseEntity<String> saveCoordi(@RequestBody SavedCoordiDto dto) {
        dto.setUserId(getCurrentUserId()); // 현재 로그인한 유저 ID 세팅
        closetService.saveRecommendation(dto); // DB 저장
        return ResponseEntity.ok("코디가 옷장에 저장되었습니다.");
    }

    // 기존 옷장에 저장된 코디 정보를 수정
    @PutMapping("/{recId}")
    public ResponseEntity<String> updateCoordi(@PathVariable("recId") Long recId, @RequestBody SavedCoordiDto dto) {
        dto.setRecId(recId); // URL 경로의 ID 세팅
        dto.setUserId(getCurrentUserId()); // 권한 확인을 위해 현재 유저 ID 세팅
        closetService.updateRecommendation(dto); // DB 수정
        return ResponseEntity.ok("코디 정보가 수정되었습니다.");
    }

    // 옷장에서 특정 코디를 삭제
    @DeleteMapping("/{recId}")
    public ResponseEntity<String> deleteCoordi(@PathVariable("recId") Long recId) {
        closetService.deleteSavedCoordi(recId, getCurrentUserId()); // DB 삭제 (본인 소유 확인 로직 포함)
        return ResponseEntity.ok("옷장에서 코디가 삭제되었습니다.");
    }
}