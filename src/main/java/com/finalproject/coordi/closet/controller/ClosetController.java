package com.finalproject.coordi.closet.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.ManualSetDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.service.ClosetService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetService closetService;

    // 현재 요청을 보낸 사용자의 ID(PK)를 SecurityContext에서 안전하게 추출하는 공통 메서드
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 존재하지 않거나 비로그인(anonymousUser) 상태인 경우 예외를 발생시켜 접근을 차단
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        User user = (User) authentication.getPrincipal();
        return Long.parseLong(user.getUsername()); 
    }

    // 옷장 메인 화면 진입 시 호출되며, 화면 렌더링에 필요한 모든 데이터를 모델에 담아 반환
    @GetMapping
    public String myCloset(Model model) {
        Long currentUserId = getCurrentUserId();
        
        // 사용자가 저장한 전체 코디(AI 추천 및 수동 조합) 목록을 조회
        List<SavedCoordiDto> allCoordis = closetService.getSavedCoordis(currentUserId);
        
        // 전체 코디 중 수동 조합이 아닌 데이터만 필터링하여 AI 추천 코디 목록으로 분류
        List<SavedCoordiDto> aiCoordis = allCoordis.stream()
                .filter(c -> !"MANUAL_SET".equals(c.getInputMode()))
                .collect(Collectors.toList());
                
        // 전체 코디 중 'MANUAL_SET'인 데이터만 필터링하여 수동 조합 코디 목록으로 분류
        List<SavedCoordiDto> manualSets = allCoordis.stream()
                .filter(c -> "MANUAL_SET".equals(c.getInputMode()))
                .collect(Collectors.toList());
        
        // 상단 프로필 영역에 출력할 사용자 기본 정보를 모델에 추가
        model.addAttribute("userId", currentUserId);
        model.addAttribute("nickname", closetService.getUserNickname(currentUserId)); 
        model.addAttribute("profileImageUrl", closetService.getUserProfileImageUrl(currentUserId)); 
        
        // 분류된 코디 목록과 개별 아이템 목록을 모델에 추가
        model.addAttribute("savedCoordis", aiCoordis);
        model.addAttribute("manualSets", manualSets);
        model.addAttribute("items", closetService.getUserCloset(currentUserId));
        
        return "closet/closet-main";
    }

    // 사용자가 업로드한 여러 장의 사진을 기반으로 옷장에 개별 아이템을 등록
    @PostMapping("/add")
    public String addItem(@ModelAttribute ClosetItemDto itemDto, 
                          @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addClosetItems(getCurrentUserId(), itemDto, imageFiles);
        return "redirect:/closet";
    }

    // 사용자가 직접 조합하여 구성한 코디 세트와 이미지를 옷장에 등록
    @PostMapping("/add-set")
    public String addSet(@ModelAttribute ManualSetDto setDto, 
                         @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addManualSet(getCurrentUserId(), setDto, imageFiles);
        return "redirect:/closet";
    }

    // 옷장에 등록된 특정 개별 아이템을 삭제 처리
    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable("itemId") Long itemId) {
        closetService.removeClosetItem(itemId, getCurrentUserId());
        return "redirect:/closet";
    }

    // 옷장에 등록된 특정 코디 세트(또는 AI 추천 코디)를 삭제 처리
    @PostMapping("/delete-set/{recId}")
    public String deleteSet(@PathVariable("recId") Long recId) {
        closetService.deleteSavedCoordi(recId, getCurrentUserId());
        return "redirect:/closet";
    }

    // 사용자 프로필 정보(닉네임, 프로필 사진)를 수정
    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam("nickname") String nickname,
                              @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        closetService.updateUserProfile(getCurrentUserId(), nickname, profileImage);
        return "redirect:/closet";
    }

    // 회원 탈퇴를 처리하고 관련된 세션 및 인증 쿠키를 파기하여 완벽한 로그아웃
    @PostMapping("/profile/withdraw")
    public String withdraw(HttpServletRequest request, HttpServletResponse response) {
        closetService.withdrawUser(getCurrentUserId());
        
        // SecurityContext 초기화 및 인증 쿠키 강제 만료 처리
        SecurityContextHolder.clearContext();
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        return "redirect:/";
    }
}