package com.finalproject.coordi.users.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.finalproject.coordi.users.annotation.LoginUser;
import com.finalproject.coordi.users.dto.UsersDto;


@Controller
public class UsersController {
    
    @GetMapping("/user/profile")
    public String userProfile(@LoginUser UsersDto loginUser, Model model){
        if (loginUser!=null) {
            model.addAttribute("user", loginUser);
        }
        return "user/profile-test";
    }
}
