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

// 옷장 관련 화면 및 기능(추가, 삭제, 프로필 관리 등)을 처리하는 컨트롤러
@Controller
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetService closetService;

    // 현재 요청 유저 식별자 추출 공통 메서드
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        User user = (User) authentication.getPrincipal();
        return Long.parseLong(user.getUsername()); 
    }

    // 옷장 메인 화면 데이터 바인딩 및 렌더링 기능
    @GetMapping
    public String myCloset(Model model) {
        Long currentUserId = getCurrentUserId();
        
        List<SavedCoordiDto> allCoordis = closetService.getSavedCoordis(currentUserId);
        
        List<SavedCoordiDto> aiCoordis = allCoordis.stream()
                .filter(c -> !"MANUAL_SET".equals(c.getInputMode()))
                .collect(Collectors.toList());
                
        List<SavedCoordiDto> manualSets = allCoordis.stream()
                .filter(c -> "MANUAL_SET".equals(c.getInputMode()))
                .collect(Collectors.toList());
        
        model.addAttribute("userId", currentUserId);
        model.addAttribute("nickname", closetService.getUserNickname(currentUserId)); 
        model.addAttribute("profileImageUrl", closetService.getUserProfileImageUrl(currentUserId)); 
        
        model.addAttribute("savedCoordis", aiCoordis);
        model.addAttribute("manualSets", manualSets);
        model.addAttribute("items", closetService.getUserCloset(currentUserId));
        
        return "closet/closet-main";
    }

    // 옷장 개별 아이템 신규 등록 제어
    @PostMapping("/add")
    public String addItem(@ModelAttribute ClosetItemDto itemDto, 
                          @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addClosetItems(getCurrentUserId(), itemDto, imageFiles);
        return "redirect:/closet";
    }

    // 개별 아이템 정보 수정 및 세트 동기화 제어
    @PostMapping("/update")
    public String updateItem(@ModelAttribute ClosetItemDto itemDto) {
        closetService.updateClosetItem(itemDto, getCurrentUserId());
        return "redirect:/closet";
    }

    // 수동 조합 코디 세트 등록 제어
    @PostMapping("/add-set")
    public String addSet(@ModelAttribute ManualSetDto setDto, 
                         @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addManualSet(getCurrentUserId(), setDto, imageFiles);
        return "redirect:/closet";
    }

    // 코디 세트 제목 수정 제어
    @PostMapping("/set/update")
    public String updateSetTitle(@RequestParam("recId") Long recId, @RequestParam("inputText") String inputText) {
        closetService.updateSavedCoordiTitle(recId, inputText, getCurrentUserId());
        return "redirect:/closet";
    }

    // 옷장 개별 아이템 삭제 제어
    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable("itemId") Long itemId) {
        closetService.removeClosetItem(itemId, getCurrentUserId());
        return "redirect:/closet";
    }

    // 코디 세트 및 추천 내역 삭제 제어
    @PostMapping("/delete-set/{recId}")
    public String deleteSet(@PathVariable("recId") Long recId) {
        closetService.deleteSavedCoordi(recId, getCurrentUserId());
        return "redirect:/closet";
    }

    // 프로필 정보(닉네임, 사진) 수정 제어
    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam("nickname") String nickname,
                              @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        closetService.updateUserProfile(getCurrentUserId(), nickname, profileImage);
        return "redirect:/closet";
    }

    // 회원 탈퇴 및 인증 세션 파기 제어
    @PostMapping("/profile/withdraw")
    public String withdraw(HttpServletRequest request, HttpServletResponse response) {
        closetService.withdrawUser(getCurrentUserId());
        
        SecurityContextHolder.clearContext();
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        return "redirect:/";
    }
}