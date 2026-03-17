package com.finalproject.coordi.admin.controller;

import com.finalproject.coordi.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin-management")
    public String adminPage(Model model) {
        model.addAttribute("tagTypes", adminService.getTagTypes());
        return "admin/admin-management";
    }

}
