package com.finalproject.coordi.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    @GetMapping("/admin-management")
    public String adminPage() {
        return "admin/admin-management";
    }

    // @GetMapping("/adminmanage/tag")
    // public String edittagPage(@RequestParam String param) {
    // return "admin/admin-edittag";
    // }

}
