package com.finalproject.coordi.admin.community.controller;

import com.finalproject.coordi.admin.community.dto.AdminCommunityDto;
import com.finalproject.coordi.admin.community.service.AdminCommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/community")
public class AdminCommunityController {

    private final AdminCommunityService adminCommunityService;

    // 관리자 페이지에서 모든 게시물을 모니터링하고 관리하기 위해 전체 게시글 목록을 조회함.
    @GetMapping("/posts")
    public ResponseEntity<List<AdminCommunityDto>> getCommunityPosts() {
        List<AdminCommunityDto> posts = adminCommunityService.getPostList();
        return ResponseEntity.ok(posts);
    }

    // 관리자가 부적절한 게시물을 일시적으로 숨길 수 있도록, 게시글의 공개 상태를 변경함.
    @PatchMapping("/posts/{postId}/visibility")
    public ResponseEntity<Void> updatePostVisibility(@PathVariable("postId") Long postId,
            @RequestBody Map<String, Boolean> payload) {
        adminCommunityService.modifyPostVisibility(postId, payload.get("isPublic"));
        return ResponseEntity.ok().build();
    }

    // 관리자가 스팸 등 불필요한 게시물을 시스템에서 영구적으로 제거할 수 있도록, 특정 게시글을 삭제함.
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        adminCommunityService.removePost(postId);
        return ResponseEntity.noContent().build();
    }
}
