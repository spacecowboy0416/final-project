package com.finalproject.coordi.board.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

// Mapper가 SELECT 결과를 담는 VO
// 게시글 + recommendation + weather 조인 결과를 담는다.
@Getter
@Setter
public class BoardPostRow {
    private Long postId;
    private Long userId;
    private String nickname;
    private String title;
    private String content;
    private Boolean isPublic;
    private Integer viewCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long recId;
    private String styleType;
    private String tpoType;
    private String aiExplanation;

    private String weatherStatus;
}