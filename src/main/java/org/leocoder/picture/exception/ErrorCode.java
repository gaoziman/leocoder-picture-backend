package org.leocoder.picture.exception;

import lombok.Getter;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-09 09:55
 * @description :
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),

    PARAMS_ERROR(40000, "请求参数错误"),

    NOT_LOGIN_ERROR(40100, "未登录"),

    NO_AUTH_ERROR(40101, "无权限"),

    NOT_FOUND_ERROR(40400, "请求数据不存在"),

    FORBIDDEN_ERROR(40300, "禁止访问"),

    SYSTEM_ERROR(50000, "系统内部异常"),

    OPERATION_ERROR(50001, "操作失败"),

    BUSINESS_ERROR(50002, "业务异常"),


    EXIST(60001, "数据已存在");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
