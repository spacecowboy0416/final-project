package com.finalproject.coordi.admin.user.service;

import com.finalproject.coordi.admin.user.domain.User;
import com.finalproject.coordi.admin.user.dto.UserResponse;
import com.finalproject.coordi.admin.user.mapper.UserMapperInter;
import com.finalproject.coordi.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserMapperInter userMapperInter;

    public List<UserResponse> getAllUsers(String role, String searchTerm, String sort) {
        List<User> users = userMapperInter.findAll(role, searchTerm, sort);
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUser(userId, role, null);

        if (updatedRows == 0) {
            throw new RuntimeException("User role update failed for userId: " + userId);
        }
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUserStatus(userId, status);

        if (updatedRows == 0) {
            throw new RuntimeException("User status update failed for userId: " + userId);
        }
    }
}
