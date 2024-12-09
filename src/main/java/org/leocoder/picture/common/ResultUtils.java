package org.leocoder.picture.common;

import org.leocoder.picture.exception.ErrorCode;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-09 09:58
 * @description : 响应工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, data, "success");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static Result<?> error(ErrorCode errorCode) {
        return new Result<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 响应
     */
    public static Result<?> error(int code, String message) {
        return new Result<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static Result<?> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), null, message);
    }
}

