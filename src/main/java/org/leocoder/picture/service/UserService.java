package org.leocoder.picture.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.User;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:02
 * @description :
 */

public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);


    String encryptPassword(String rawPassword);
}
