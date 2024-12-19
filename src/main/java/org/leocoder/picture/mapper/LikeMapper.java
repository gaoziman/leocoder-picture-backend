package org.leocoder.picture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.leocoder.picture.domain.Like;

/**
 * @author : 程序员Leo
 * @date  2024-12-18 21:21
 * @version 1.0
 * @description :
 */

public interface LikeMapper extends BaseMapper<Like> {
    /**
     * 根据用户ID和图片ID查询是否已点赞
     *
     * @param userId    用户id
     * @param pictureId 图片id
     * @return UserLike
     */
    Like findByUserIdAndPictureId(@Param("userId") Long userId, @Param("pictureId") Long pictureId);

    /**
     * 根据用户ID和图片ID删除点赞记录
     * @param userId     用户id
     * @param pictureId 图片id
     */
    void deleteByUserIdAndPictureId(@Param("userId") Long userId, @Param("pictureId") Long pictureId);
}