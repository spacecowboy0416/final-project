package com.finalproject.coordi.admin.user.service;

import com.finalproject.coordi.admin.user.domain.User;
import com.finalproject.coordi.admin.user.dto.UserResponse;
import com.finalproject.coordi.admin.user.dto.UserUpdateRequest;
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
    public void updateUser(Long userId, UserUpdateRequest request) {
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUser(userId, request.getRole(), request.getStatus());

        if (updatedRows == 0) {
            // This might happen if the provided data is the same as the existing data,
            // or if the user ID does not exist.
        }
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUser(userId, null, status);

        if (updatedRows == 0) {
            throw new RuntimeException("User status update failed for userId: " + userId);
        }
    }
}
