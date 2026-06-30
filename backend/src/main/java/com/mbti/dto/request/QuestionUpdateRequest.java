package com.mbti.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionUpdateRequest {
    @NotBlank(message = "题目 ID 不能为空")
    private String id;

    @NotBlank(message = "题目标题不能为空")
    private String title;

    @Min(value = 1, message = "排序号至少为 1")
    private int sortOrder;

    @JsonProperty("isActive")
    private boolean isActive;

    @NotEmpty(message = "选项不能为空")
    @Size(min = 2, message = "至少需要 2 个选项")
    private List<QuestionCreateRequest.OptionRequest> options;
}
