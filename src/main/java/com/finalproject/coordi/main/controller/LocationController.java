package com.finalproject.coordi.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finalproject.coordi.main.dto.LocationResponse;
import com.finalproject.coordi.main.service.LocationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/api/location/region")
    public LocationResponse getRegion(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon
    ) {
        return locationService.getRegion(lat, lon);
    }
}