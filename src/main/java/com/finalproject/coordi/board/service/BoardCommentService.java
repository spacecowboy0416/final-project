package com.finalproject.coordi.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finalproject.coordi.board.dto.request.BoardCommentCreateRequest;
import com.finalproject.coordi.board.dto.request.BoardCommentUpdateRequest;
import com.finalproject.coordi.board.dto.response.BoardCommentResponse;
import com.finalproject.coordi.board.mapper.BoardCommentMapper;
import com.finalproject.coordi.board.mapper.BoardPostMapper;
import com.finalproject.coordi.board.vo.BoardCommentRow;
import com.finalproject.coordi.exception.board.BoardCommentNotFoundException;
import com.finalproject.coordi.exception.board.BoardForbiddenException;
import com.finalproject.coordi.exception.board.BoardPostNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentMapper boardCommentMapper;
    private final BoardPostMapper boardPostMapper;

    // 댓글 등록
    @Transactional
    public void createComment(Long postId, Long loginUserId, BoardCommentCreateRequest request) {
        Integer postExists = boardPostMapper.existsActivePost(postId);
        if (postExists == null || postExists == 0) {
            throw new BoardPostNotFoundException();
        }

        boardCommentMapper.insertBoardComment(postId, loginUserId, request.content());
        boardCommentMapper.increaseCommentCount(postId);
    }

	// 댓글 목록 조회
	@Transactional(readOnly = true)
	public List<BoardCommentResponse> getComments(Long postId, Long loginUserId) {
	    Integer postExists = boardPostMapper.existsActivePost(postId);
	    if (postExists == null || postExists == 0) {
	        throw new BoardPostNotFoundException();
	    }
	
	    return boardCommentMapper.findCommentsByPostId(postId)
	            .stream()
	            .map(row -> toResponse(row, loginUserId))
	            .toList();
	}
    
	 // 댓글 수정
	 // 작성자 본인만 수정 가능
	 @Transactional
	 public void updateComment(Long commentId, Long loginUserId, BoardCommentUpdateRequest request) {
	     Integer exists = boardCommentMapper.existsComment(commentId);
	     if (exists == null || exists == 0) {
	         throw new BoardCommentNotFoundException();
	     }
	
	     Long authorId = boardCommentMapper.findCommentAuthorId(commentId);
	
	     if (authorId == null) {
	         throw new BoardCommentNotFoundException();
	     }
	     if (!authorId.equals(loginUserId)) {
	         throw new BoardForbiddenException();
	     }
	
	     int updated = boardCommentMapper.updateBoardComment(commentId, request.content());
	     if (updated == 0) {
	         throw new BoardCommentNotFoundException();
	     }
	 }

    // 댓글 삭제
    // 작성자 본인만 삭제 가능
    @Transactional
    public void deleteComment(Long commentId, Long loginUserId) {
        Integer exists = boardCommentMapper.existsComment(commentId);
        if (exists == null || exists == 0) {
            throw new BoardCommentNotFoundException();
        }

        Long authorId = boardCommentMapper.findCommentAuthorId(commentId);

        if (authorId == null) {
            throw new BoardCommentNotFoundException();
        }
        if (!authorId.equals(loginUserId)) {
            throw new BoardForbiddenException();
        }

        Long postId = boardCommentMapper.findPostIdByCommentId(commentId);
        if (postId == null) {
            throw new BoardCommentNotFoundException();
        }

        int deleted = boardCommentMapper.softDeleteComment(commentId);
        if (deleted == 0) {
            throw new BoardCommentNotFoundException();
        }

        boardCommentMapper.decreaseCommentCount(postId);
    }
    
    // 게시글 삭제 시 연결된 댓글 전체 물리 삭제
    @Transactional
    public void deleteCommentsByPostId(Long postId) {
        boardCommentMapper.deleteCommentsByPostId(postId);
    }
    
    // comment VO -> Response DTO 변환
    private BoardCommentResponse toResponse(BoardCommentRow row, Long loginUserId) {
        boolean mine = row.getUserId() != null && row.getUserId().equals(loginUserId);
        boolean deleted = row.getDeletedAt() != null;
        boolean edited = row.getUpdatedAt() != null
                && row.getCreatedAt() != null
                && !row.getUpdatedAt().equals(row.getCreatedAt());
        
        // 🔥 추가 (삭제된 댓글이면 문구 변경)
        String content = deleted ? "삭제된 댓글입니다." : row.getContent();

        return new BoardCommentResponse(
                row.getCommentId(),
                row.getPostId(),
                row.getUserId(),
                row.getNickname(),
                content,
                row.getCreatedAt(),
                row.getUpdatedAt(),
                mine,
                deleted,
                edited
        );
    }
}