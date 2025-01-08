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
import org.leocoder.picture.domain.Space;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.space.SpaceAddRequest;
import org.leocoder.picture.domain.dto.space.SpaceQueryRequest;
import org.leocoder.picture.domain.vo.space.SpaceVO;
import org.leocoder.picture.domain.vo.user.UserVO;
import org.leocoder.picture.enums.SpaceLevelEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.mapper.SpaceMapper;
import org.leocoder.picture.service.SpaceService;
import org.leocoder.picture.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-30 20:45
 * @description :
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    private final TransactionTemplate transactionTemplate;

    private final UserService userService;


    /**
     * 校验空间名称、空间级别是否合法
     *
     * @param space 空间对象
     * @param add   是否是新增
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }


    /**
     * 新增空间
     *
     * @param requestParam 新增空间请求参数
     * @param loginUser    登录用户
     * @return 新增空间的 id
     */
    @Override
    public long addSpace(SpaceAddRequest requestParam, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(requestParam, space);
        // 默认值
        if (StrUtil.isBlank(requestParam.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (ObjectUtil.isNull(requestParam.getSpaceLevel())) {
            requestParam.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 填充数据
        this.fillSpaceBySpaceLevel(space);
        // 数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        // 权限校验 - 只有仅本人才可以进行创建空间，并且只能创建普通的空间
        if (SpaceLevelEnum.COMMON.getValue() != requestParam.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(40102, "无权限创建指定级别的空间");
        }
        // 针对用户进行加锁
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
                // 写入数据库
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                // 返回新写入的数据 id
                return space.getId();
            });
            // 返回结果是包装类，可以做一些处理
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }


    /**
     * 根据空间级别，自动填充限额
     *
     * @param space 空间对象
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (ObjectUtil.isNotNull(spaceLevelEnum)) {
            long maxSize = spaceLevelEnum.getMaxSize();
            // 为空才进行填充，不为空则不进行填充
            if (ObjectUtil.isNull(space.getMaxSize())) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (ObjectUtil.isNull(space.getMaxCount())) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 通用的查询条件
     *
     * @param requestParam 查询请求参数
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest requestParam) {
        if (ObjectUtil.isNull(requestParam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = requestParam.getId();
        Long userId = requestParam.getUserId();
        String spaceName = requestParam.getSpaceName();
        Integer spaceLevel = requestParam.getSpaceLevel();
        String sortField = requestParam.getSortField();
        String sortOrder = requestParam.getSortOrder();


        LambdaQueryWrapper<Space> lambdaQueryWrapper = Wrappers.lambdaQuery(Space.class);

        // 条件查询
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(id), Space::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(userId), Space::getUserId, userId);
        lambdaQueryWrapper.like(StrUtil.isNotBlank(spaceName), Space::getSpaceLevel, spaceName);
        lambdaQueryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);


        // 排序
        lambdaQueryWrapper.orderBy(ObjUtil.isNotEmpty(sortField),
                "ascend".equals(sortOrder),
                sortField != null ? Space::getSpaceName : Space::getId);

        return lambdaQueryWrapper;
    }


    /**
     * 分页查询空间列表
     *
     * @param spacePage 分页对象
     * @param request   请求对象
     * @return 分页查询结果
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> pictureVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> pictureVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
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
     * 获取封装类
     *
     * @param space   空间对象
     * @param request 请求对象
     * @return 空间对象
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }
}
