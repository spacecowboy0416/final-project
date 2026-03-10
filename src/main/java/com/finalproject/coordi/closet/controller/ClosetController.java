package com.finalproject.coordi.closet.controller;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.service.ClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetService closetService;

    @GetMapping
    public String myCloset(Model model) {
        Long currentUserId = 1L; // 임시 유저 ID (Spring Security 연동 필요)
        
        // 내 옷장에 코디 카드와 단품 옷 리스트 모두 전달
        model.addAttribute("savedCoordis", closetService.getSavedCoordis(currentUserId));
        model.addAttribute("items", closetService.getUserCloset(currentUserId));
        
        return "closet/closet-main";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute ClosetItemDto itemDto, 
                          @RequestParam("imageFile") MultipartFile imageFile) {
        Long currentUserId = 1L;
        closetService.addClosetItem(currentUserId, itemDto, imageFile);
        return "redirect:/closet";
    }

    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        Long currentUserId = 1L;
        closetService.removeClosetItem(itemId, currentUserId);
        return "redirect:/closet";
    }
}