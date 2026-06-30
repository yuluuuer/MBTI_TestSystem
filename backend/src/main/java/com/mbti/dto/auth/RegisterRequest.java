package com.mbti.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "账号不能为空")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少 6 位")
    private String password;

    @Size(max = 50, message = "昵称最多 50 个字符")
    private String name;

    @Size(max = 20, message = "手机号最多 20 个字符")
    private String phone;

    private String gender;

    private LocalDate birthDate;
}
