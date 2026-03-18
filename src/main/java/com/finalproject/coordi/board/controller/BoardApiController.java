package com.finalproject.coordi.board.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.finalproject.coordi.board.dto.request.BoardCommentCreateRequest;
import com.finalproject.coordi.board.dto.request.BoardPostCreateRequest;
import com.finalproject.coordi.board.dto.request.BoardPostUpdateRequest;
import com.finalproject.coordi.board.dto.response.BoardCommentResponse;
import com.finalproject.coordi.board.dto.response.BoardPostCreateResponse;
import com.finalproject.coordi.board.dto.response.BoardPostDetailResponse;
import com.finalproject.coordi.board.dto.response.BoardPostListResponse;
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
    // 로그인한 사용자의 userId를 @LoginUser로 받아 서비스에 전달
    @PostMapping
    public BoardPostCreateResponse createPost(
            @LoginUser UsersDto loginUser,
            @RequestBody @Valid BoardPostCreateRequest request
    ) {
        return boardPostService.createPost(loginUser.getUserId(), request);
    }

    // 게시글 목록 조회
    // weather/style/tpo 필터를 선택적으로 받는다.
    @GetMapping
    public BoardPostListResponse getPostList(
            @RequestParam(name = "weather", required = false) String weather,
            @RequestParam(name = "style", required = false) String style,
            @RequestParam(name = "tpo", required = false) String tpo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size
    ) {
        return boardPostService.getPostList(weather, style, tpo, page, size);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public BoardPostDetailResponse getPostDetail(@PathVariable Long postId) {
        return boardPostService.getPostDetail(postId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public void updatePost(
            @PathVariable Long postId,
            @LoginUser UsersDto loginUser,
            @RequestBody @Valid BoardPostUpdateRequest request
    ) {
        boardPostService.updatePost(postId, loginUser.getUserId(), request);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(
            @PathVariable Long postId,
            @LoginUser UsersDto loginUser
    ) {
        boardPostService.deletePost(postId, loginUser.getUserId());
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public void createComment(
            @PathVariable Long postId,
            @LoginUser UsersDto loginUser,
            @RequestBody @Valid BoardCommentCreateRequest request
    ) {
        boardCommentService.createComment(postId, loginUser.getUserId(), request);
    }

    // 댓글 목록 조회
    @GetMapping("/{postId}/comments")
    public List<BoardCommentResponse> getComments(@PathVariable Long postId) {
        return boardCommentService.getComments(postId);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @PathVariable Long commentId,
            @LoginUser UsersDto loginUser
    ) {
        boardCommentService.deleteComment(commentId, loginUser.getUserId());
    }
}