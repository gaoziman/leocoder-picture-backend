package org.leocoder.picture.domain.dto.user;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-17 22:29
 * @description : 用户密码请求对象
 */
@Data
public class UserPasswordRequest {
    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String checkPassword;
}
