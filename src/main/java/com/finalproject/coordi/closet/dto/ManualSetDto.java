package com.finalproject.coordi.closet.dto;

import lombok.Data;
import java.util.List;

@Data
public class ManualSetDto {
    private String setName;
    private String season;
    private List<String> setItemNames;
    private List<Long> setCategoryIds;
    private List<String> setBrands;
    private List<String> setColors;
    private List<String> setMaterials;
    private List<String> setFits;
    private List<String> setStyles;
}