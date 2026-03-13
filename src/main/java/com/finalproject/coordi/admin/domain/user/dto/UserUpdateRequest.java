package com.finalproject.coordi.admin.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String role;
    private String status;
}
