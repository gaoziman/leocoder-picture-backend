package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.user.*;
import org.leocoder.picture.domain.vo.user.LoginUserVO;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:05
 * @description : 用户相关接口
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Api(tags = "登录管理")
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

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public Result<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userLoginRequest), ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }


    @ApiOperation(value = "获取登录用户信息")
    @GetMapping("/get/login")
    public Result<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    @ApiOperation(value = "用户注销")
    @PostMapping("/logout")
    public Result<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @ApiOperation(value = "修改用户信息")
    @PostMapping("/updateUserInfo")
    public Result<Boolean> updateUserInfo(@RequestBody UserInfoRequest requestParam,HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        userService.updateUserInfo(requestParam,request);
        return ResultUtils.success(true);
    }


    @ApiOperation(value = "修改用户密码")
    @PostMapping("/updateUserPassword")
    public Result<Boolean> updateUserPassword(@RequestBody UserPasswordRequest requestParam, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        userService.updateUserPassword(requestParam,request);
        return ResultUtils.success(true);
    }
}
