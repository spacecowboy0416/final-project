package com.finalproject.coordi.admin.domain.user;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class User {
    private Long userId;
    private String provider;
    private String providerUserId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
