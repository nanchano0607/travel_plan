package com.min.edu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}$",
            message = "비밀번호는 8~64자이며, 대문자, 소문자, 숫자, 특수문자를 각각 1자 이상 포함하고 공백은 사용할 수 없습니다."
    )
    @Size(min = 8, max = 64)
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;
    private String phone;
}