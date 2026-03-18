package com.finalproject.coordi.main.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationResponse {
    private String city;
    private String gu;
    private String dong;
}