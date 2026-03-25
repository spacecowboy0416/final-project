package com.finalproject.coordi.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finalproject.coordi.board.vo.BoardCommentRow;

// XML과 연결되는 인터페이스
@Mapper
public interface BoardCommentMapper {

    // 댓글 등록
    int insertBoardComment(
            @Param("postId") Long postId,
            @Param("userId") Long userId,
            @Param("content") String content
    );

    // 게시글별 댓글 목록 조회
    List<BoardCommentRow> findCommentsByPostId(@Param("postId") Long postId);

    // 댓글 작성자 조회
    Long findCommentAuthorId(@Param("commentId") Long commentId);

    // 댓글이 속한 게시글 ID 조회
    Long findPostIdByCommentId(@Param("commentId") Long commentId);
    
	// 댓글 수정
	int updateBoardComment(
	        @Param("commentId") Long commentId,
	        @Param("content") String content
	);

    // 댓글 존재 여부 확인
    Integer existsComment(@Param("commentId") Long commentId);

    // 댓글 소프트 삭제
    int softDeleteComment(@Param("commentId") Long commentId);

    // 게시글에 달린 댓글 전체 삭제
    int deleteCommentsByPostId(@Param("postId") Long postId);

    // 게시글의 comment_count 증가
    int increaseCommentCount(@Param("postId") Long postId);

    // 게시글의 comment_count 감소
    int decreaseCommentCount(@Param("postId") Long postId);
}