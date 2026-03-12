package com.finalproject.coordi.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finalproject.coordi.main.dto.MainResponse;
import com.finalproject.coordi.main.dto.WeatherResponse;
import com.finalproject.coordi.main.service.MainService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainController {

    private final MainService mainService;

    @GetMapping("/summary")
    public MainResponse getSummary(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(name = "isDefault", defaultValue = "false") boolean isDefault
    ) {
        return mainService.getSummary(lat, lon, isDefault);
    }

    @PostMapping("/preview")
    public MainResponse preview(@RequestBody WeatherResponse weather) {
        return mainService.preview(weather);
    }
}