package com.example.vibecoding.common;

public enum ErrorCode {
    PARAM_VALIDATION_FAILED(40001, "参数校验失败"),
    REQUEST_PARAM_ERROR(40002, "请求参数格式/类型错误"),
    PROJECT_NOT_FOUND(40401, "项目不存在"),
    INTERNAL_SERVER_ERROR(50000, "服务内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}