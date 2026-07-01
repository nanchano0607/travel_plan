package com.min.edu.auth.dto;

import com.min.edu.auth.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "변경할 권한은 필수입니다")
    private Role role;

    public UpdateRoleRequest(Role role) {
        this.role = role;
    }
}
