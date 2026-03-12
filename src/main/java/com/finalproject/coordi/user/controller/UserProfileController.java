package com.finalproject.coordi.user.controller;

import com.finalproject.coordi.user.annotation.LoginUser;
import com.finalproject.coordi.user.dto.UserDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 커스텀 어노테이션으로 로그인 유저 정보 가져오기
@Controller
public class UserProfileController {

    @GetMapping("/user/profile")
    public String myProfile(@LoginUser UserDto user, Model model) {

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "user/profile";
    }
}
