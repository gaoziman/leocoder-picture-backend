package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.picture.PictureQueryRequest;
import org.leocoder.picture.domain.dto.picture.PictureReviewRequest;
import org.leocoder.picture.domain.dto.picture.PictureUploadByBatchRequest;
import org.leocoder.picture.domain.dto.picture.PictureUploadRequest;
import org.leocoder.picture.domain.vo.picture.PictureVO;

import javax.servlet.http.HttpServletRequest;

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
     * @param inputSource        上传文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片信息
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


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
     * @param requestParam  批量抓取请求参数
     * @param loginUser 登录用户
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest requestParam, User loginUser);

}
