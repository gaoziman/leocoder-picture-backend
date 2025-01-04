package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.annotation.AuthCheck;
import org.leocoder.picture.common.DeleteRequest;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.constant.UserConstant;
import org.leocoder.picture.domain.Space;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.picture.PictureEditRequest;
import org.leocoder.picture.domain.dto.space.SpaceAddRequest;
import org.leocoder.picture.domain.dto.space.SpaceQueryRequest;
import org.leocoder.picture.domain.dto.space.SpaceUpdateRequest;
import org.leocoder.picture.domain.vo.space.SpaceLevel;
import org.leocoder.picture.domain.vo.space.SpaceVO;
import org.leocoder.picture.enums.SpaceLevelEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.SpaceService;
import org.leocoder.picture.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-30 23:29
 * @description ：空间管理
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "空间管理")
@RequestMapping("/space")
public class SpaceController {
    private final SpaceService spaceService;

    private final UserService userService;


    @PostMapping("/add")
    @ApiOperation(value = "新增空间")
    public Result<Long> addSpace(@RequestBody SpaceAddRequest requestParam, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(requestParam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 操作数据库
        long spaceId = spaceService.addSpace(requestParam, loginUser);
        return ResultUtils.success(spaceId);
    }


    @ApiOperation(value = "分页获取空间列表（封装类）")
    @PostMapping("/list/page/vo")
    public Result<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest requestParam, HttpServletRequest request) {
        long current = requestParam.getPageNum();
        long size = requestParam.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getLambdaQueryWrapper(requestParam));
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }


    @PostMapping("/delete")
    @ApiOperation(value = "删除空间")
    public Result<Boolean> deleteSpace(@RequestBody DeleteRequest requestParam, HttpServletRequest request) {
        if (ObjectUtil.isNull(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = requestParam.getId();
        // 判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(oldSpace), ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "更新空间信息（管理员使用）")
    public Result<Boolean> updateSpace(@RequestBody SpaceUpdateRequest requestParam) {
        if (ObjectUtil.isEmpty(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(requestParam, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        long id = requestParam.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldSpace), ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @ApiOperation(value = "编辑空间信息（给用户使用）")
    @PostMapping("/edit")
    public Result<Boolean> editSpace(@RequestBody PictureEditRequest requestParam, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(requestParam, space);
        // 设置编辑时间
        space.setEditTime(LocalDateTime.now());
        // 数据校验
        spaceService.validSpace(space,false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = requestParam.getId();
        Space oldPicture = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @ApiOperation(value = "根据 id 获取空间信息（仅管理员可用）")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }


    @ApiOperation(value = "根据 id 获取空间信息（封装类）")
    @GetMapping("/get/vo")
    public Result<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // 查询数据库中的图片信息
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    @GetMapping("/list/level")
    @ApiOperation(value = "获取空间等级列表")
    public Result<List<SpaceLevel>> listSpaceLevel() {
        // 获取所有枚举
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

}
