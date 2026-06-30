package com.mbti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 50, message = "昵称长度 1-50 个字符")
    private String name;
}
