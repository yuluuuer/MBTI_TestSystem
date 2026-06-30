package com.mbti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamTypeCreateRequest {
    @NotBlank(message = "名称不能为空")
    @Size(max = 50, message = "名称最多 50 个字符")
    private String name;

    @Size(max = 1000, message = "描述最多 1000 个字符")
    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationMinutes;

    private Integer questionLimit;

    private List<String> participantIds;
}
