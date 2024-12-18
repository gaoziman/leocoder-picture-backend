package org.leocoder.picture.domain.dto.user;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-17 22:39
 * @description : 用户信息请求对象
 */
@Data
public class UserInfoRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;
}
