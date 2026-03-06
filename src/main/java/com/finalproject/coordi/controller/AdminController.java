package com.finalproject.coordi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/adminmanage")
    public String adminPage() {
        return "admin/admin-managing";
    }
}
