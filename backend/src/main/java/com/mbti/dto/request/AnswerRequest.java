package com.mbti.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    @NotBlank(message = "questionId 不能为空")
    private String questionId;

    @NotBlank(message = "optionId 不能为空")
    private String optionId;
}
