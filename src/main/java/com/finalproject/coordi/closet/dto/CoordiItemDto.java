package com.finalproject.coordi.closet.dto;

import lombok.Data;

@Data
public class CoordiItemDto {
    private Long recItemId;
    private String slotKey;
    private String sourceType;
    private Long closetItemId;
    private Long productId;
    private String imageUrl;
    private String name;
}