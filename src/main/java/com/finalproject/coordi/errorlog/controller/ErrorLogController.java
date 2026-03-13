package com.finalproject.coordi.errorlog.controller;

import com.finalproject.coordi.errorlog.mapper.ErrorLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ErrorLogController {

    private final ErrorLogMapper errorLogMapper;

    @GetMapping("/admin/logs")
    public String adminLogs(Model model) {
        model.addAttribute("logs", errorLogMapper.findAllLogs());
        return "admin-error-logs"; 
    }
}