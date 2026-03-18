package com.finalproject.coordi.closet.controller;

import java.util.List;
import java.util.stream.Collectors;

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

    // 현재 로그인한 사용자의 식별자(PK)를 가져오는 공통 메서드
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("로그인이 필요한 서비스입니다.");
        }

        User user = (User) authentication.getPrincipal();
        return Long.parseLong(user.getUsername()); 
    }

    // 옷장 메인 화면 진입 및 데이터 바인딩
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
        
        model.addAttribute("savedCoordis", aiCoordis);
        model.addAttribute("manualSets", manualSets);
        model.addAttribute("items", closetService.getUserCloset(currentUserId));
        
        return "closet/closet-main";
    }

    // 옷장에 개별 아이템 등록
    @PostMapping("/add")
    public String addItem(@ModelAttribute ClosetItemDto itemDto, 
                          @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addClosetItems(getCurrentUserId(), itemDto, imageFiles);
        return "redirect:/closet";
    }

    // 옷장에 코디 세트 등록
    @PostMapping("/add-set")
    public String addSet(@ModelAttribute ManualSetDto setDto, 
                         @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addManualSet(getCurrentUserId(), setDto, imageFiles);
        return "redirect:/closet";
    }

    // 개별 아이템 삭제 처리
    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable("itemId") Long itemId) {
        closetService.removeClosetItem(itemId, getCurrentUserId());
        return "redirect:/closet";
    }

    // 코디 세트(또는 AI 코디 룩북) 삭제 처리 후 화면 새로고침
    @PostMapping("/delete-set/{recId}")
    public String deleteSet(@PathVariable("recId") Long recId) {
        closetService.deleteSavedCoordi(recId, getCurrentUserId());
        return "redirect:/closet"; // 삭제 후 옷장 메인으로 깔끔하게 새로고침!
    }
}