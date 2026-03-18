package com.finalproject.coordi.admin.user.controller;

import com.finalproject.coordi.admin.user.dto.UserResponse;
import com.finalproject.coordi.admin.user.dto.UserRoleUpdateRequest;
import com.finalproject.coordi.admin.user.dto.UserStatusUpdateRequest;
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

    // 관리자가 역할, 검색어, 정렬 기준에 따라 사용자를 필터링하고 조회할 수 있는 동적인 목록 기능을 제공하기 위함.
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "id_desc") String sort) {
        List<UserResponse> users = adminUserService.getAllUsers(role, searchTerm, sort);
        return ResponseEntity.ok(users);
    }

    // 관리자가 특정 사용자를 관리자나 다른 역할로 변경할 수 있도록 사용자 권한 수정 기능을 제공함.
    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateRequest request) {
        adminUserService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok().build();
    }

    // 관리자가 비활성 또는 정지된 사용자의 계정 상태를 변경할 수 있도록 사용자 상태 수정 기능을 제공함.
    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequest request) {
        adminUserService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok().build();
    }
}
