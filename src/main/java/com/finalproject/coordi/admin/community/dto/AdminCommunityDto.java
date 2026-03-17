package com.finalproject.coordi.admin.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCommunityDto {
    private Long postId;
    private String title;
    private String authorNickname;
    private String authorEmail;
    private LocalDateTime createdAt;
    private boolean isPublic;
    private int commentCount;
    private Long recId;
}
