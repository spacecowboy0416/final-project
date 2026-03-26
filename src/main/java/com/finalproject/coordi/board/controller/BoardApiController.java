package com.finalproject.coordi.board.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.finalproject.coordi.board.dto.request.BoardCommentCreateRequest;
import com.finalproject.coordi.board.dto.request.BoardCommentUpdateRequest;
import com.finalproject.coordi.board.dto.request.BoardPostCreateRequest;
import com.finalproject.coordi.board.dto.request.BoardPostUpdateRequest;
import com.finalproject.coordi.board.dto.response.BoardCommentResponse;
import com.finalproject.coordi.board.dto.response.BoardPostCreateResponse;
import com.finalproject.coordi.board.dto.response.BoardPostDetailResponse;
import com.finalproject.coordi.board.dto.response.BoardPostListResponse;
import com.finalproject.coordi.board.dto.response.BoardSavedCoordiResponse;
import com.finalproject.coordi.board.service.BoardCommentService;
import com.finalproject.coordi.board.service.BoardPostService;
import com.finalproject.coordi.users.annotation.LoginUser;
import com.finalproject.coordi.users.dto.UsersDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/board/posts")
@RequiredArgsConstructor
@Validated
public class BoardApiController {

    private final BoardPostService boardPostService;
    private final BoardCommentService boardCommentService;

    // 게시글 작성
    @PostMapping
    public BoardPostCreateResponse createPost(
            @LoginUser UsersDto loginUser,
            @RequestBody @Valid BoardPostCreateRequest request
    ) {
        return boardPostService.createPost(loginUser.getUserId(), request);
    }

    // 저장 코디 목록 조회
    @GetMapping("/coordis")
    public List<BoardSavedCoordiResponse> getSavedCoordis(
            @LoginUser UsersDto loginUser
    ) {
        return boardPostService.getSavedCoordis(loginUser.getUserId());
    }

    // 게시글 목록 조회
    @GetMapping
    public BoardPostListResponse getPostList(
            @RequestParam(name = "weather", required = false) String weather,
            @RequestParam(name = "style", required = false) String style,
            @RequestParam(name = "tpo", required = false) String tpo,
            @RequestParam(name = "sort", defaultValue = "latest") String sort,
            @RequestParam(name = "mine", defaultValue = "false") boolean mine,  
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @LoginUser UsersDto loginUser
    ) {
    	Long loginUserId = (loginUser != null) ? loginUser.getUserId() : null;
        return boardPostService.getPostList(weather, style, tpo, sort, mine, page, size, loginUserId);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public BoardPostDetailResponse getPostDetail(
            @PathVariable("postId") Long postId,
            @LoginUser UsersDto loginUser
    ) {
        Long loginUserId = (loginUser != null) ? loginUser.getUserId() : null;
        return boardPostService.getPostDetail(postId, loginUserId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public void updatePost(
            @PathVariable("postId") Long postId,
            @LoginUser UsersDto loginUser,
            @RequestBody @Valid BoardPostUpdateRequest request
    ) {
        boardPostService.updatePost(postId, loginUser.getUserId(), request);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(
            @PathVariable("postId") Long postId,
            @LoginUser UsersDto loginUser
    ) {
        boardPostService.deletePost(postId, loginUser.getUserId());
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable("postId") Long postId,
            @LoginUser UsersDto loginUser,
            @RequestBody @Valid BoardCommentCreateRequest request
    ) {
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boardCommentService.createComment(postId, loginUser.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    // 댓글 목록 조회
    @GetMapping("/{postId}/comments")
    public List<BoardCommentResponse> getComments(
            @PathVariable("postId") Long postId,
            @LoginUser UsersDto loginUser
    ) {
        Long loginUserId = (loginUser != null) ? loginUser.getUserId() : null;
        return boardCommentService.getComments(postId, loginUserId);
    }

	// 댓글 수정
	@PutMapping("/comments/{commentId}")
	public void updateComment(
	        @PathVariable("commentId") Long commentId,
	        @LoginUser UsersDto loginUser,
	        @RequestBody @Valid BoardCommentUpdateRequest request
	) {
	    boardCommentService.updateComment(commentId, loginUser.getUserId(), request);
	}

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @PathVariable("commentId") Long commentId,
            @LoginUser UsersDto loginUser
    ) {
        boardCommentService.deleteComment(commentId, loginUser.getUserId());
    }
    
}