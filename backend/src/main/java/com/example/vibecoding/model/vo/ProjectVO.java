package com.example.vibecoding.model.vo;

import com.example.vibecoding.model.enums.ProjectStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * 项目响应视图对象
 * 用于Controller层返回给前端的数据结构
 */
@Data
public class ProjectVO {
    private Long id;
    private String name;
    private String owner;
    private ProjectStatusEnum status;
    private Date createdAt;
    private Date updatedAt;
    private Integer deleted;
}