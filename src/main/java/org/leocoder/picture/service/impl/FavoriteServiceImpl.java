package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.domain.Favorite;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.domain.dto.favorite.FavoriteRequest;
import org.leocoder.picture.domain.vo.favorite.FavoritePictureVO;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.mapper.FavoriteMapper;
import org.leocoder.picture.mapper.PictureMapper;
import org.leocoder.picture.service.FavoriteService;
import org.leocoder.picture.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description :
 */

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {


    private final PictureMapper pictureMapper;


    /**
     * 收藏图片
     *
     * @param requestParam 收藏请求参数
     * @return 是否收藏成功
     */
    @Override
    public boolean addFavorite(FavoriteRequest requestParam) {
        Long userId = requestParam.getUserId();
        Long pictureId = requestParam.getPictureId();
        // 查询是否已经收藏过
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("picture_id", pictureId);
        queryWrapper.eq("is_favorited", 1);
        Favorite oldFavorite = this.getOne(queryWrapper);
        if (ObjectUtil.isNotNull(oldFavorite)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "已经收藏过了，不能重复收藏");
        }
        // 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPictureId(pictureId);
        favorite.setIsFavorited(1);
        // 更新图片的收藏数
        pictureMapper.incrementFavoriteCount(pictureId);
        return save(favorite);
    }


    /**
     * 取消收藏
     *
     * @param requestParam 取消收藏请求参数
     * @return 是否取消收藏成功
     */
    @Override
    public boolean removeFavorite(FavoriteRequest requestParam) {
        Long userId = requestParam.getUserId();
        Long pictureId = requestParam.getPictureId();
        // 删除收藏记录
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("picture_id", pictureId)
                .eq("is_favorited", 1);
        // 更新图片的收藏数
        pictureMapper.decrementFavoriteCount(pictureId);
        return remove(queryWrapper);
    }



    /**
     * 获取用户收藏的图片列表
     *
     * @param userId 用户id
     * @return 收藏的图片列表
     */
    @Override
    public List<FavoritePictureVO> getFavoritePictureList(Long userId) {
        // 查询用户收藏列表
        LambdaQueryWrapper<Favorite> queryWrapper = Wrappers.lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getIsFavorited, 1);
        List<Favorite> favorites = this.list(queryWrapper);

        // 如果没有收藏记录，直接返回空列表
        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询对应的图片信息
        List<Long> pictureIds = favorites.stream()
                .map(Favorite::getPictureId)
                .collect(Collectors.toList());

        List<Picture> pictures = pictureMapper.selectBatchIds(pictureIds);

        // 将收藏和图片信息封装为 VO
        Map<Long, Picture> pictureMap = pictures.stream()
                .collect(Collectors.toMap(Picture::getId, Function.identity()));

        return favorites.stream()
                .map(favorite -> {
                    Picture picture = pictureMap.get(favorite.getPictureId());
                    if (ObjectUtil.isNotNull(picture)) {
                        FavoritePictureVO vo = new FavoritePictureVO();
                        vo.setPictureId(picture.getId());
                        vo.setUrl(picture.getUrl());
                        vo.setName(picture.getName());
                        vo.setIntroduction(picture.getIntroduction());
                        // 安全解析 tags 字段
                        if (picture.getTags() != null && !picture.getTags().isEmpty()) {
                            vo.setTags(JsonUtils.parseTags(picture.getTags()));
                        } else {
                            vo.setTags(new ArrayList<>()); // tags 为空时设置为空列表
                        }
                        vo.setCategory(picture.getCategory());
                        return vo;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取用户收藏的图片列表
     *
     * @param userId   用户id
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页收藏的图片列表
     */
    @Override
    public Page<FavoritePictureVO> getFavoritePicturePage(Long userId, long pageNum, long pageSize) {
        // 查询用户收藏分页数据
        LambdaQueryWrapper<Favorite> queryWrapper = Wrappers.lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getIsFavorited, 1)
                // 按收藏时间降序排序
                .orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favoritePage = new Page<>(pageNum, pageSize);
        this.page(favoritePage, queryWrapper);

        // 如果没有收藏记录，直接返回空分页
        if (favoritePage.getRecords().isEmpty()) {
            return new Page<>();
        }

        // 查询对应的图片信息
        List<Long> pictureIds = favoritePage.getRecords().stream()
                .map(Favorite::getPictureId)
                .collect(Collectors.toList());
        List<Picture> pictures = pictureMapper.selectBatchIds(pictureIds);

        // 将收藏和图片信息封装为 VO
        Map<Long, Picture> pictureMap = pictures.stream()
                .collect(Collectors.toMap(Picture::getId, Function.identity()));
        List<FavoritePictureVO> favoritePictureVOList = favoritePage.getRecords().stream()
                .map(favorite -> {
                    Picture picture = pictureMap.get(favorite.getPictureId());
                    if (ObjectUtil.isNotNull(picture)) {
                        FavoritePictureVO vo = new FavoritePictureVO();
                        vo.setPictureId(picture.getId());
                        vo.setUrl(picture.getUrl());
                        vo.setName(picture.getName());
                        vo.setIntroduction(picture.getIntroduction());
                        // 安全解析 tags 字段
                        if (picture.getTags() != null && !picture.getTags().isEmpty()) {
                            vo.setTags(JsonUtils.parseTags(picture.getTags()));
                        } else {
                            vo.setTags(new ArrayList<>()); // tags 为空时设置为空列表
                        }
                        vo.setCategory(picture.getCategory());
                        return vo;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 将分页数据转换为 VO 分页
        Page<FavoritePictureVO> favoritePictureVOPage = new Page<>();
        favoritePictureVOPage.setCurrent(favoritePage.getCurrent());
        favoritePictureVOPage.setSize(favoritePage.getSize());
        favoritePictureVOPage.setTotal(favoritePage.getTotal());
        favoritePictureVOPage.setRecords(favoritePictureVOList);

        return favoritePictureVOPage;
    }
}
