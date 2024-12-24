package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.favorite.FavoriteQueryRequest;
import org.leocoder.picture.domain.dto.favorite.FavoriteRequest;
import org.leocoder.picture.domain.vo.favorite.FavoritePictureVO;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.FavoriteService;
import org.leocoder.picture.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description : 用户收藏Controller
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Api(tags = "用户收藏")
public class FavoriteController {

    private final FavoriteService favoriteService;

    private final UserService userService;


    @ApiOperation("收藏图片")
    @PostMapping("/add")
    public Result<Boolean> addFavorite(@RequestBody FavoriteRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        requestParam.setUserId(loginUser.getId());
        boolean result = favoriteService.addFavorite(requestParam);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"收藏失败");
        return  ResultUtils.success(true);
    }


    @ApiOperation("取消收藏图片")
    @PostMapping("/remove")
    public Result<Boolean> removeFavorite(@RequestBody FavoriteRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        requestParam.setUserId(loginUser.getId());
        boolean result = favoriteService.removeFavorite(requestParam);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"取消收藏失败");
        return  ResultUtils.success(true);
    }



    @ApiOperation("查询用户的收藏列表")
    @PostMapping("/list")
    public Result<Page<FavoritePictureVO>> favoriteList(@RequestBody FavoriteQueryRequest requestParam, HttpServletRequest request) {
        long pageNum = requestParam.getPageNum();
        long pageSize = requestParam.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        // 查询收藏列表分页
        Page<FavoritePictureVO> favoritePicturePage = favoriteService.getFavoritePicturePage(userId, pageNum, pageSize);

        // 返回封装结果
        return ResultUtils.success(favoritePicturePage);
    }
}
