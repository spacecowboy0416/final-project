package com.finalproject.coordi.admin.controller;

import com.finalproject.coordi.admin.dto.TagDto;

import com.finalproject.coordi.admin.service.AdminService;
import com.finalproject.coordi.domain.user.dto.UserResponse;
import com.finalproject.coordi.domain.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "id_desc") String sort) {
        List<UserResponse> users = adminService.getAllUsers(role, searchTerm, sort);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        adminService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    // Tag Management Endpoints
    @GetMapping("/tags/types")
    public ResponseEntity<List<String>> getTagTypes() {
        return ResponseEntity.ok(adminService.getTagTypes());
    }

    @GetMapping("/tags")
    public ResponseEntity<List<TagDto>> getTagsByType(@RequestParam("type") String type) {
        return ResponseEntity.ok(adminService.getTagsByType(type));
    }

    @SuppressWarnings("null")
    @PostMapping("/tags")
    public ResponseEntity<TagDto> addTag(@RequestBody TagDto tag) {
        TagDto newTag = adminService.addTag(tag);
        return ResponseEntity.created(URI.create("/api/admin/tags/" + newTag.getTagId())).body(newTag);
    }

    @PutMapping("/tags/{tagId}")
    public ResponseEntity<Void> updateTag(@PathVariable Long tagId, @RequestBody Map<String, String> payload) {
        adminService.updateTag(tagId, payload.get("name"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        adminService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
