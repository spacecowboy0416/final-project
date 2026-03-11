package com.finalproject.coordi.admin.service;

import com.finalproject.coordi.domain.user.User;
import com.finalproject.coordi.domain.user.UserRepository;
import com.finalproject.coordi.domain.user.dto.UserResponse;
import com.finalproject.coordi.domain.user.dto.UserUpdateRequest;
import com.finalproject.coordi.domain.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers(String role, String searchTerm, String sort) {
        List<User> users = userRepository.findAll(role, searchTerm, sort);
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userRepository.updateUser(userId, request.getRole(), request.getStatus());

        if (updatedRows == 0) {
            // This might happen if the provided data is the same as the existing data,
            // or if the user ID does not exist. The findById check should handle the latter.
            // Depending on requirements, this could be a no-op or an error.
            // For now, we'll treat it as a potential issue to log or handle.
            // Consider what should happen if no rows are updated.
        }
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        int updatedRows = userRepository.updateUser(userId, null, status);

        if (updatedRows == 0) {
            throw new RuntimeException("User status update failed for userId: " + userId);
        }
    }
}
