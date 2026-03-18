package com.finalproject.coordi.admin.user.controller;

import com.finalproject.coordi.admin.user.dto.UserResponse;
import com.finalproject.coordi.admin.user.dto.UserUpdateRequest;
import com.finalproject.coordi.admin.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "id_desc") String sort) {
        List<UserResponse> users = adminUserService.getAllUsers(role, searchTerm, sort);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        adminUserService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }
}
