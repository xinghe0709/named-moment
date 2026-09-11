package com.example.namedmoment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    PARAM_ERROR(40001, "参数错误"),
    AUTH_REQUIRED(40101, "请先登录"),
    AUTH_FAILED(40102, "用户名或密码不正确"),
    USERNAME_TAKEN(40901, "这个用户名已经被使用"),
    AUTH_RATE_LIMITED(42901, "尝试次数过多，请稍后再试"),
    RECORD_NOT_FOUND(40401, "情感记录不存在"),
    FINGERPRINT_FAILED(50001, "情感指纹生成失败"),
    CONCEPT_MATCH_FAILED(50002, "概念匹配失败"),
    DATABASE_ERROR(50003, "数据库访问失败");

    private final int code;
    private final String message;
}
