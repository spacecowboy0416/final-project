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

    private Long getCurrentUserId() {
        return 1L; // TODO: 추후 Spring Security 유저 정보로 변경
    }

    @GetMapping
    public String myCloset(Model model) {
        Long currentUserId = getCurrentUserId();
        
        model.addAttribute("savedCoordis", closetService.getSavedCoordis(currentUserId));
        model.addAttribute("items", closetService.getUserCloset(currentUserId));
        
        return "closet/closet-main";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute ClosetItemDto itemDto, 
                          @RequestParam("imageFile") MultipartFile imageFile) {
        closetService.addClosetItem(getCurrentUserId(), itemDto, imageFile);
        return "redirect:/closet";
    }

    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        closetService.removeClosetItem(itemId, getCurrentUserId());
        return "redirect:/closet";
    }
}