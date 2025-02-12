package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.common.DeleteRequest;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.picture.*;
import org.leocoder.picture.domain.vo.picture.PictureVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-12 23:10
 * @description :
 */

public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource          上传文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片信息
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 删除图片校验
     *
     * @param loginUser 登录用户
     * @param picture   图片信息
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询请求参数
     * @return LambdaQueryWrapper
     */
    LambdaQueryWrapper<Picture> getLambdaQueryWrapper(PictureQueryRequest requestParam);


    /**
     * 获取图片信息封装类
     *
     * @param picture 图片信息
     * @param request 请求对象
     * @return 图片信息封装类
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);


    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间id
     * @param picColor  颜色
     * @param loginUser 登录用户
     * @return 图片信息封装类
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 分页获取图片信息封装类
     *
     * @param picturePage 分页对象
     * @param request     请求对象
     * @return 分页图片信息封装类
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    void validPicture(Picture picture);


    /**
     * 图片审核
     *
     * @param requestParam 图片审核请求参数
     * @param loginUser    登录用户
     */
    void doPictureReview(PictureReviewRequest requestParam, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);


    /**
     * 批量抓取和创建图片
     *
     * @param requestParam 批量抓取请求参数
     * @param loginUser    登录用户
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest requestParam, User loginUser);


    /**
     * 获取图片浏览次数
     *
     * @param pictureId 图片ID
     * @return 当前图片的浏览次数
     */
    Long getViewCount(Long pictureId);

    /**
     * 增加图片浏览次数
     *
     * @param pictureId 图片ID
     */
    void incrementViewCount(Long pictureId);


    /**
     * 增加图片浏览次数，缓存版本
     *
     * @param pictureId 图片ID
     */
    void incrementViewCountInCache(Long pictureId);


    /**
     * 分页获取图片信息封装类，带缓存
     *
     * @param requestParam 图片查询请求参数
     * @param request      请求对象
     * @return 分页图片信息封装类
     */
    Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest requestParam, HttpServletRequest request);


    /**
     * 管理员手动刷新缓存
     *
     * @param requestParam 图片查询请求参数
     * @return 刷新是否成功
     */
    boolean refreshCache(PictureQueryRequest requestParam, HttpServletRequest request);


    /**
     * 编辑图片信息（用户使用）
     *
     * @param requestParam 图片编辑请求参数
     * @param request      请求对象
     */
    void editPicture(PictureEditRequest requestParam, HttpServletRequest request);


    /**
     * 批量删除图片
     *
     * @param requestParam 批量删除请求参数
     * @param request      请求对象
     */
    void deleteBatchPicture(DeleteBatchRequest requestParam, HttpServletRequest request);


    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求参数
     * @param request       请求对象
     */
    void deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);


    /**
     * 更新图片信息（管理员使用）
     *
     * @param requestParam 图片更新请求参数
     * @param request      请求对象
     */
    void updatePicture(PictureUpdateRequest requestParam, HttpServletRequest request);


    /**
     * 批量编辑图片信息（管理员使用）
     *
     * @param requestParam 图片批量编辑请求参数
     * @param loginUser    登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest requestParam, User loginUser);
}
