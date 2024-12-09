package org.leocoder.picture.common;

import lombok.Data;
import org.leocoder.picture.exception.ErrorCode;

import java.io.Serializable;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-09 09:57
 * @description : 统一返回结果
 */
@Data
public class Result<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Result(int code, T data) {
        this(code, data, "");
    }

    public Result(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
