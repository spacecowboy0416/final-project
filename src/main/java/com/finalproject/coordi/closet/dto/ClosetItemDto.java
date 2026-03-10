package com.finalproject.coordi.closet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosetItemDto {
	private Long itemId;
    private Long userId;
    private Long categoryId;
    private String name;
    private String color;
    private String season;
    private Integer thickness;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String categoryName;
}