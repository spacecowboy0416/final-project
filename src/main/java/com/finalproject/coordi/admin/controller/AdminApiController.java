package com.finalproject.coordi.admin.controller;

import com.finalproject.coordi.admin.service.AdminService;
import com.finalproject.coordi.domain.user.dto.UserResponse;
import com.finalproject.coordi.domain.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
