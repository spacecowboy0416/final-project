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

    private Long weatherId;
    private String weatherStatus;
    private Double temp;
    private Double feelsLike;
    private Integer humidity;
    private Double windSpeed;
    private String placeName;

    // 게시판 목록용 대표 코디 정보
    private String topItemName;
    private String bottomItemName;
    
    // 게시판 목록용 코디 이미지 4개
    private String topItemImageUrl;
    private String bottomItemImageUrl;
    private String outerItemImageUrl;
    private String shoesItemImageUrl;
}