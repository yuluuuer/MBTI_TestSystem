package com.mbti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DimensionUpdateRequest {
    @NotBlank(message = "维度代码不能为空")
    private String code;

    @NotBlank(message = "维度名称不能为空")
    @Size(max = 20, message = "名称最多 20 个字符")
    private String name;

    @NotBlank(message = "描述不能为空")
    @Size(max = 2000, message = "描述最多 2000 个字符")
    private String description;

    @NotBlank(message = "examTypeId 不能为空")
    private String examTypeId;
}
