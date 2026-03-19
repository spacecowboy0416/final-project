package com.finalproject.coordi.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// SSR 페이지 진입용 컨트롤러
// 실제 데이터는 Fetch API로 /api/board/... 에서 가져온다.
@Controller
public class BoardPageController {

    // 게시판 목록 페이지
    @GetMapping("/board")
    public String boardListPage() {
        return "board/board-list";
    }

    // 게시글 작성 페이지
    @GetMapping("/board/write")
    public String boardWritePage() {
        return "board/board-write";
    }

    // 게시글 상세 페이지
    @GetMapping("/board/{postId}")
    public String boardDetailPage(@PathVariable("postId") Long postId) {
        return "board/board-detail";
    }

    // 게시글 수정 페이지
    @GetMapping("/board/{postId}/edit")
    public String boardEditPage(@PathVariable("postId") Long postId) {
        return "board/board-edit";
    }
}