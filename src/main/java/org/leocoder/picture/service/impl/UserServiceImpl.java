package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.enums.UserRoleEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.mapper.UserMapper;
import org.leocoder.picture.service.UserService;
import org.leocoder.picture.utils.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:02
 * @description :
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能小于8位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }

        // 2. 检验用户是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUserAccount, userAccount);
        User existingUser = this.getOne(lambdaQueryWrapper);
        if (ObjectUtil.isNotNull(existingUser)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }

        // 3. 密码加密
        String encryptedPassword = encryptPassword(userPassword);


        // 4.保存用户信息
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptedPassword)
                .userRole(UserRoleEnum.USER.getValue())
                .userName("无名用户")
                .build();

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return user.getId();
    }

    /**
     * 对密码进行加盐加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    @Override
    public String encryptPassword(String rawPassword) {
        String salt = PasswordUtil.generateFixedSalt("leocoder");
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(salt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和盐值不能为空");
        }
        // 将密码和盐值拼接
        String saltedPassword = rawPassword + salt;
        // 使用 MD5 进行加密
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes());
    }

}
