package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.annotation.AuthCheck;
import org.leocoder.picture.common.DeleteRequest;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.constant.UserConstant;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.domain.Space;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.picture.*;
import org.leocoder.picture.domain.vo.picture.PictureVO;
import org.leocoder.picture.enums.PictureReviewStatusEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.PictureService;
import org.leocoder.picture.service.SpaceService;
import org.leocoder.picture.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-12 23:13
 * @description :
 */
@RestController
@RequestMapping("/picture")
@RequiredArgsConstructor
@Api(tags = "图片管理")
public class PictureController {

    private final PictureService pictureService;

    private final UserService userService;

    private final SpaceService spaceService;


    @ApiOperation(value = "上传图片（可重新上传）")
    @PostMapping("/upload")
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    @ApiOperation(value = "url上传图片")
    public Result<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    @ApiOperation(value = "删除图片")
    @PostMapping("/delete")
    @Transactional
    public Result<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.deletePicture(deleteRequest, request);

        return ResultUtils.success(true);
    }

    @ApiOperation(value = "批量删除图片")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/deleteBatchPicture")
    public Result<Boolean> deleteBatchPicture(@RequestBody DeleteBatchRequest requestParam, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(requestParam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.deleteBatchPicture(requestParam, request);
        return ResultUtils.success(true);
    }


    @ApiOperation(value = "更新图片（仅管理员可用）")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> updatePicture(@RequestBody PictureUpdateRequest requestParam, HttpServletRequest request) {
        if (ObjectUtil.isNull(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.updatePicture(requestParam, request);
        return ResultUtils.success(true);
    }


    @ApiOperation(value = "根据 id 获取图片（仅管理员可用）")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }


    @ApiOperation(value = "分页获取图片列表（按热度排序）")
    @PostMapping("/list/page/popular")
    public Result<Page<PictureVO>> listPopularPictures(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getPageNum();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 按浏览次数降序排序
        LambdaQueryWrapper<Picture> queryWrapper = pictureService.getLambdaQueryWrapper(pictureQueryRequest).orderByDesc(Picture::getViewCount);
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), queryWrapper);

        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    @ApiOperation(value = "根据 id 获取图片（封装类）")
    @GetMapping("/get/vo")
    public Result<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库中的图片信息
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND_ERROR);

        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        if (ObjectUtil.isNotNull(spaceId)) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }

        // 如果图片为待审核状态，直接返回数据，不增加浏览次数
        if (PictureReviewStatusEnum.REVIEWING.getValue() == picture.getReviewStatus() && picture.getSpaceId() == null) {
            return ResultUtils.success(pictureService.getPictureVO(picture, request));
        }
        // 增加浏览次数
        pictureService.incrementViewCountInCache(id);

        // 获取最新的浏览量
        Long viewCount = pictureService.getViewCount(id);
        // 更新浏览量
        picture.setViewCount(viewCount);

        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    @ApiOperation(value = "获取图片浏览次数（Redis实时的数据）")
    @GetMapping("/{id}/viewCount")
    public Result<Long> getPictureViewCount(@PathVariable Long id) {
        Long viewCount = pictureService.getViewCount(id);
        return ResultUtils.success(viewCount);
    }


    @ApiOperation(value = "分页获取图片列表（仅管理员可用）")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getPageNum();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getLambdaQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }


    @ApiOperation(value = "分页获取图片列表（封装类）")
    @PostMapping("/list/page/vo")
    public Result<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest requestParam, HttpServletRequest request) {
        long current = requestParam.getPageNum();
        long size = requestParam.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 空间权限校验
        Long spaceId = requestParam.getSpaceId();
        // 公开图库
        if (ObjectUtil.isNull(spaceId)) {
            // 普通用户默认只能查看已过审的公开数据
            requestParam.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            requestParam.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getLambdaQueryWrapper(requestParam));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    @ApiOperation(value = "分页获取图片列表(带缓存)")
    @PostMapping("/list/page/vo/cache")
    public Result<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest requestParam, HttpServletRequest request) {
        Page<PictureVO> picturePage = pictureService.listPictureVOByPageWithCache(requestParam, request);
        return ResultUtils.success(picturePage);
    }


    @ApiOperation(value = "刷新缓存（仅管理员可用）")
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/refreshCache")
    public Result<Boolean> refreshCache(@RequestBody PictureQueryRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);

        // 调用服务层方法刷新缓存
        boolean refreshResult = pictureService.refreshCache(requestParam, request);
        ThrowUtils.throwIf(!refreshResult, ErrorCode.OPERATION_ERROR, "缓存刷新失败");

        return ResultUtils.success(true);
    }


    @ApiOperation(value = "分页获取已发布图片列表（封装类）")
    @PostMapping("/list/page/user")
    public Result<Page<PictureVO>> listPictureVOByUser(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getPageNum();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        pictureQueryRequest.setUserId(userService.getLoginUser(request).getId());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getLambdaQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    @ApiOperation(value = "编辑图片（给用户使用）")
    @PostMapping("/edit")
    public Result<Boolean> editPicture(@RequestBody PictureEditRequest requestParam, HttpServletRequest request) {
        if (ObjectUtil.isNull(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.editPicture(requestParam, request);
        return ResultUtils.success(true);

    }


    @PostMapping("/review")
    @ApiOperation(value = "审核图片")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> doPictureReview(@RequestBody PictureReviewRequest requestParam, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(requestParam, loginUser);
        return ResultUtils.success(true);
    }

    @ApiOperation(value = "批量导入图片")
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(requestParam, loginUser);
        return ResultUtils.success(uploadCount);
    }


    @ApiOperation(value = "获取相邻图片")
    @PostMapping("/adjacent")
    public Result<Map<String, Long>> getAdjacentPictures(@RequestParam Long pictureId,
                                                         @RequestParam String sortField,
                                                         @RequestParam String sortOrder,
                                                         @RequestParam String from, // 新增参数
                                                         HttpServletRequest request) {
        // 查询当前图片
        Picture currentPicture = pictureService.getById(pictureId);
        if (currentPicture == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        // 获取当前用户登录信息
        User loginUser = userService.getLoginUser(request);

        // 判断是否为公开图片（没有 SpaceId 的图片视为公开图片）
        boolean isPublicPicture = currentPicture.getSpaceId() == null;

        // 如果来源为“我的空间”，校验权限
        if ("space".equals(from)) {
            if (!isPublicPicture) {
                Space space = spaceService.getById(currentPicture.getSpaceId());
                if (space == null || !loginUser.getId().equals(space.getUserId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问该空间");
                }
            }
        }

        // 查询上一张图片
        LambdaQueryChainWrapper<Picture> prevQuery = pictureService.lambdaQuery()
                .lt("createTime".equals(sortField) ? Picture::getCreateTime : Picture::getViewCount,
                        "createTime".equals(sortField) ? currentPicture.getCreateTime() : currentPicture.getViewCount())
                .eq(Picture::getReviewStatus, PictureReviewStatusEnum.PASS.getValue());

        // 根据来源限制查询范围
        if ("space".equals(from)) {
            // 如果是我的空间，限定空间范围
            prevQuery.eq(Picture::getSpaceId, currentPicture.getSpaceId());
        } else if ("public".equals(from)) {
            // 如果是主页，只查询公开图片
            prevQuery.isNull(Picture::getSpaceId);
        }

        if ("descend".equals(sortOrder)) {
            prevQuery.orderByDesc("createTime".equals(sortField) ? Picture::getCreateTime : Picture::getViewCount);
        } else {
            prevQuery.orderByAsc("createTime".equals(sortField) ? Picture::getCreateTime : Picture::getViewCount);
        }

        Long prevId = prevQuery.last("LIMIT 1").oneOpt().map(Picture::getId).orElse(null);

        // 查询下一张图片
        LambdaQueryChainWrapper<Picture> nextQuery = pictureService.lambdaQuery()
                .gt("createTime".equals(sortField) ? Picture::getCreateTime : Picture::getViewCount,
                        "createTime".equals(sortField) ? currentPicture.getCreateTime() : currentPicture.getViewCount())
                .eq(Picture::getReviewStatus, PictureReviewStatusEnum.PASS.getValue());

        if ("space".equals(from)) {
            nextQuery.eq(Picture::getSpaceId, currentPicture.getSpaceId());
        } else if ("public".equals(from)) {
            nextQuery.isNull(Picture::getSpaceId);
        }

        if ("descend".equals(sortOrder)) {
            nextQuery.orderByDesc("createTime".equals(sortField) ? Picture::getCreateTime : Picture::getViewCount);
        } else {
            nextQuery.orderByAsc("createTime".equals(sortField) ? Picture::getCreateTime : Picture::getViewCount);
        }

        Long nextId = nextQuery.last("LIMIT 1").oneOpt().map(Picture::getId).orElse(null);

        // 返回结果
        Map<String, Long> result = new HashMap<>();
        result.put("prevId", nextId);
        result.put("nextId", prevId);
        return ResultUtils.success(result);
    }
}
