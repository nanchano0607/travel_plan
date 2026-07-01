package com.min.edu.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    private String name;

    @Size(min = 2, max = 100, message = "닉네임은 2~100자여야 합니다.")
    private String nickname;

    private String phone;
}
