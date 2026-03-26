package com.finalproject.coordi.closet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordiItemDto {
    private Long recItemId;
    private String slotKey;
    private String sourceType;
    private Long closetItemId;
    private Long productId;
    private String imageUrl;
    private String name;
    private String brand;
    private String color;
    private String categoryName;
}