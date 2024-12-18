package org.leocoder.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.file.UploadPictureResult;
import org.leocoder.picture.domain.dto.picture.PictureQueryRequest;
import org.leocoder.picture.domain.dto.picture.PictureReviewRequest;
import org.leocoder.picture.domain.dto.picture.PictureUploadRequest;
import org.leocoder.picture.domain.vo.picture.PictureVO;
import org.leocoder.picture.domain.vo.user.UserVO;
import org.leocoder.picture.enums.PictureReviewStatusEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.manager.FileManager;
import org.leocoder.picture.mapper.PictureMapper;
import org.leocoder.picture.service.PictureService;
import org.leocoder.picture.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-12 23:10
 * @description :
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    private final FileManager fileManager;

    private final UserService userService;


    /**
     * 上传图片
     *
     * @param multipartFile 上传文件
     * @param requestParam  图片上传请求
     * @param loginUser     登录用户
     * @return 图片信息
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest requestParam, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (requestParam != null) {
            pictureId = requestParam.getId();
        }

        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // 上传图片，得到信息
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId);
        // 补充审核参数
        fillReviewParams(picture, loginUser);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }


    /**
     * 获取图片信息
     *
     * @param loginUser           登录用户
     * @param uploadPictureResult 上传图片结果
     * @param pictureId           图片 id
     * @return 图片信息
     */
    private static Picture getPicture(User loginUser, UploadPictureResult uploadPictureResult, Long pictureId) {
        Picture picture = Picture.builder().build();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(LocalDateTime.now());
        }
        return picture;
    }


    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询请求参数
     * @return LambdaQueryWrapper
     */
    @Override
    public LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest requestParam) {
        if (ObjectUtil.isNull(requestParam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = requestParam.getId();
        String name = requestParam.getName();
        String introduction = requestParam.getIntroduction();
        String category = requestParam.getCategory();
        List<String> tags = requestParam.getTags();
        Long picSize = requestParam.getPicSize();
        Integer picWidth = requestParam.getPicWidth();
        Integer picHeight = requestParam.getPicHeight();
        Double picScale = requestParam.getPicScale();
        String picFormat = requestParam.getPicFormat();
        String searchText = requestParam.getSearchText();
        Long userId = requestParam.getUserId();
        String sortField = requestParam.getSortField();
        String sortOrder = requestParam.getSortOrder();
        Long reviewerId = requestParam.getReviewerId();
        String reviewMessage = requestParam.getReviewMessage();
        Integer reviewStatus = requestParam.getReviewStatus();

        LambdaQueryWrapper<Picture> lambdaQueryWrapper = Wrappers.lambdaQuery(Picture.class);

        // 多字段模糊搜索
        if (StrUtil.isNotBlank(searchText)) {
            lambdaQueryWrapper.and(qw ->
                    qw.like(Picture::getName, searchText)
                            .or()
                            .like(Picture::getIntroduction, searchText)
            );
        }

        // 条件查询
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Picture::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Picture::getName, name);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(reviewMessage), Picture::getReviewMessage, reviewMessage);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(category), Picture::getCategory, category);
        lambdaQueryWrapper.eq(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus);

        // JSON 数组查询（标签）
        if (ObjUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                lambdaQueryWrapper.like(Picture::getTags, "\"" + tag + "\"");
            }
        }

        // 排序
        lambdaQueryWrapper.orderBy(ObjUtil.isNotEmpty(sortField),
                "ascend".equals(sortOrder),
                sortField != null ? Picture::getName : Picture::getId);

        return lambdaQueryWrapper;
    }


    /**
     * 获取图片信息封装类
     *
     * @param picture 图片信息
     * @param request 请求对象
     * @return 图片信息封装类
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片信息封装类
     *
     * @param picturePage 分页对象
     * @param request     请求对象
     * @return 分页图片信息封装类
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 图片审核
     *
     * @param requestParam 图片审核请求参数
     * @param loginUser    登录用户
     */
    @Override
    public void doPictureReview(PictureReviewRequest requestParam, User loginUser) {
        // 1. 校验参数
        Long id = requestParam.getId();
        Integer reviewStatus = requestParam.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 判断是否存在
        Picture oldPicture = this.getById(id);
        if (ObjectUtil.isNull(oldPicture)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }

        // 判断是否是改状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "请勿重复审核");
        }

        // 3. 更新图片审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(requestParam, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(LocalDateTime.now());
        boolean result = this.saveOrUpdate(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片审核失败");
    }


    /**
     * 填充审核参数
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(LocalDateTime.now());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

}
