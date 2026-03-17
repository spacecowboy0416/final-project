package com.finalproject.coordi.users.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Alias("udto")
public class UsersDto {
    private Long userId;
    private String provider;
    private String providerUserId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String status;
    private Timestamp createdAt;
    private Timestamp lastLoginAt;
    
    // 신규 가입자 여부 확인용 (필드 아님)
    private boolean isNewUser;
}
