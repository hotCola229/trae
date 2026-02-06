package com.example.vibecoding.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatusEnum {
    DRAFT(0, "草稿"),
    ACTIVE(1, "激活"),
    ARCHIVED(2, "归档");

    @EnumValue
    @JsonValue
    private final int value;
    private final String description;

    ProjectStatusEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ProjectStatusEnum fromValue(int value) {
        for (ProjectStatusEnum status : ProjectStatusEnum.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid project status value: " + value);
    }
}