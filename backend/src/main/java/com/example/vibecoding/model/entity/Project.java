package com.example.vibecoding.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.vibecoding.model.enums.ProjectStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("project")
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("owner")
    private String owner;

    @TableField("status")
    private ProjectStatusEnum status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}