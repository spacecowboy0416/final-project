package com.finalproject.coordi.closet.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SavedCoordiDto {
    private Long recId;
    private Long userId;
    private Long weatherId;
    private String inputMode;
    private String inputText;
    private String productOption;
    private String tpoType;
    private String styleType;
    private Boolean isSaved;
    private String aiBlueprint;
    private String aiExplanation;
    private LocalDateTime createdAt;
}
