package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.like.LikeRequest;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.LikeService;
import org.leocoder.picture.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description : 用户点赞控制器
 */
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
@Api(tags = "用户点赞")
public class LikeController {

    private final LikeService likeService;

    private final UserService userService;


    @ApiOperation("点赞图片")
    @PostMapping("/like")
    public Result<Boolean> like(@RequestBody LikeRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long pictureId = requestParam.getPictureId();
        Integer likeType = requestParam.getLikeType();
        Boolean isLiked = requestParam.getIsLiked();

        boolean result = likeService.toggleLike(loginUser.getId(), pictureId,likeType,isLiked);
        ThrowUtils.throwIf(!result, ErrorCode.BUSINESS_ERROR, "您已点赞过该图片");
        return ResultUtils.success(true);
    }

    @ApiOperation("取消点赞图片")
    @PostMapping("/cancelLike")
    public Result<Boolean> cancelLike(@RequestBody LikeRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long pictureId = requestParam.getPictureId();
        Integer likeType = requestParam.getLikeType();
        Boolean isLiked = requestParam.getIsLiked();

        boolean result = likeService.toggleLike(loginUser.getId(), pictureId,likeType,isLiked);
        //
        ThrowUtils.throwIf(!result, ErrorCode.BUSINESS_ERROR, "您未点赞过该图片");
        return ResultUtils.success(true);
    }

}
