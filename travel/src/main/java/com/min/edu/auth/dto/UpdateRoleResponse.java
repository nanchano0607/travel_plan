package com.min.edu.auth.dto;

import com.min.edu.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateRoleResponse {

    private Long userId;
    private String email;
    private Role role;
}
