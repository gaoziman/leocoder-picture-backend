package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.domain.dto.UserRegisterRequest;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:05
 * @description :
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Api(tags = "用户管理")
public class UserController {

    private final UserService userService;


    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest requestParam) {
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        String userAccount = requestParam.getUserAccount();
        String userPassword = requestParam.getUserPassword();
        String checkPassword = requestParam.getCheckPassword();
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }
}
