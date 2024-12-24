package org.leocoder.picture.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.Favorite;
import org.leocoder.picture.domain.dto.favorite.FavoriteRequest;
import org.leocoder.picture.domain.vo.favorite.FavoritePictureVO;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description :
 */

public interface FavoriteService extends IService<Favorite> {

    /**
     * 收藏图片
     *
     * @param requestParam 收藏请求参数
     * @return 是否收藏成功
     */
    boolean addFavorite(FavoriteRequest requestParam);

    /**
     * 取消收藏
     *
     * @param requestParam 取消收藏请求参数
     * @return 是否取消收藏成功
     */
    boolean removeFavorite(FavoriteRequest requestParam);


    /**
     * 获取用户收藏的图片列表
     *
     * @param userId 用户id
     * @return 收藏的图片列表
     */
    List<FavoritePictureVO> getFavoritePictureList(Long userId);


    /**
     * 分页获取用户收藏的图片列表
     *
     * @param userId   用户id
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页收藏的图片列表
     */
    Page<FavoritePictureVO> getFavoritePicturePage(Long userId, long pageNum, long pageSize);
}
