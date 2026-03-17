package com.finalproject.coordi.closet.controller;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.ManualSetDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.service.ClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetService closetService;

    // 임시 유저 식별자 로직 (추후 Security 적용 시 변경 필요)
    private Long getCurrentUserId() {
        return 1L; 
    }

    // 옷장 메인 화면 진입 처리
    @GetMapping
    public String myCloset(Model model) {
        Long currentUserId = getCurrentUserId();
        
        List<SavedCoordiDto> allCoordis = closetService.getSavedCoordis(currentUserId);
        
        // AI 추천 룩북과 수동 등록 세트 분리
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

    // 개별 아이템 다중 등록 처리
    @PostMapping("/add")
    public String addItem(@ModelAttribute ClosetItemDto itemDto, 
                          @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addClosetItems(getCurrentUserId(), itemDto, imageFiles);
        return "redirect:/closet";
    }

    // 코디 세트 등록 처리
    @PostMapping("/add-set")
    public String addSet(@ModelAttribute ManualSetDto setDto, 
                         @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        closetService.addManualSet(getCurrentUserId(), setDto, imageFiles);
        return "redirect:/closet";
    }

    // 아이템 삭제 처리
    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        closetService.removeClosetItem(itemId, getCurrentUserId());
        return "redirect:/closet";
    }
}