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

    @GetMapping("/posts")
    public ResponseEntity<List<AdminCommunityDto>> getCommunityPosts() {
        List<AdminCommunityDto> posts = adminCommunityService.getPostList();
        return ResponseEntity.ok(posts);
    }

    @PatchMapping("/posts/{postId}/visibility")
    public ResponseEntity<Void> updatePostVisibility(@PathVariable Long postId,
            @RequestBody Map<String, Boolean> payload) {
        adminCommunityService.modifyPostVisibility(postId, payload.get("isPublic"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminCommunityService.removePost(postId);
        return ResponseEntity.noContent().build();
    }
}
