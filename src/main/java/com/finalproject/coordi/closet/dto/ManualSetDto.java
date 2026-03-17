package com.finalproject.coordi.closet.dto;

import lombok.Data;
import java.util.List;

@Data
public class ManualSetDto {
    private String setName;
    private String season;
    private List<String> setItemNames;
    private List<Long> setCategoryIds;
}