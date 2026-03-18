package com.finalproject.coordi.admin.controller;

import com.finalproject.coordi.admin.tag.service.AdminTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminTagService adminTagService;

    // 관리자 메뉴 진입, 진입 시 유저 관리 탭 자동 연결
    @GetMapping("/admin-management")
    public String adminPage(Model model) {
        model.addAttribute("tagTypes", adminTagService.getTagTypes());
        return "admin/admin-management";
    }

}
