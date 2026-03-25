package com.finalproject.coordi.board.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

// 댓글 조회 결과를 담는 VO
@Getter
@Setter
public class BoardCommentRow {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}