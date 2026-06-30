package com.mbti.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitRequest {
    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;
}
