package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.Space;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.space.SpaceAddRequest;
import org.leocoder.picture.domain.dto.space.SpaceQueryRequest;
import org.leocoder.picture.domain.vo.space.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-30 20:45
 * @description :
 */

public interface SpaceService extends IService<Space> {


    /**
     * 校验空间名称、空间级别是否合法
     *
     * @param space 空间对象
     * @param add   是否是新增
     */
    void validSpace(Space space, boolean add);


    /**
     * 新增空间
     *
     * @param requestParam 新增空间请求参数
     * @param loginUser    登录用户
     * @return
     */
    long addSpace(SpaceAddRequest requestParam, User loginUser);

    /**
     * 根据空间级别，自动填充限额
     *
     * @param space 空间对象
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 通用的查询条件
     *
     * @param requestParam 查询请求参数
     * @return 查询条件
     */
    LambdaQueryWrapper<Space> getLambdaQueryWrapper(SpaceQueryRequest requestParam);


    /**
     * 分页查询空间列表
     *
     * @param spacePage 分页对象
     * @param request   请求对象
     * @return 分页查询结果
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 获取封装类
     *
     * @param space   空间对象
     * @param request 请求对象
     * @return 空间对象
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);
}
