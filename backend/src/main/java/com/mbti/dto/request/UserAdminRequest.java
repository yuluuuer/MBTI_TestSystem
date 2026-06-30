package com.mbti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserAdminRequest {
    @NotBlank(message = "userId 不能为空")
    private String userId;

    private String action; // "resetPassword", "changePassword" or "updateProfile"

    private String email;

    private String password;

    @Pattern(regexp = "USER|ADMIN|", message = "角色无效")
    private String role;

    @Size(min = 6, message = "新密码至少 6 位")
    private String newPassword;

    @Size(max = 50, message = "姓名最多 50 个字符")
    private String name;

    @Size(max = 20, message = "手机号最多 20 个字符")
    private String phone;

    @Pattern(regexp = "男|女|", message = "性别无效")
    private String gender;

    private LocalDate birthDate;

    private Boolean isActive;
}
