package com.example.vibecoding.model.request;

import com.example.vibecoding.model.enums.ProjectStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProjectUpdateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 50, message = "项目名称长度必须在1-50个字符之间")
    private String name;

    @Size(max = 50, message = "项目负责人长度不能超过50个字符")
    private String owner;

    @NotNull(message = "项目状态不能为空")
    private ProjectStatusEnum status;
}