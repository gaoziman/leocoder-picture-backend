package org.leocoder.picture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.leocoder.picture.domain.Like;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
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
    Like findByUserIdAndPictureId(@Param("userId") Long userId, @Param("pictureId") Long pictureId,@Param("likeType") Integer likeType);

    /**
     * 根据用户ID和图片ID删除点赞记录
     *
     * @param userId    用户id
     * @param pictureId 图片id
     */
    void deleteByUserIdAndPictureId(@Param("userId") Long userId, @Param("pictureId") Long pictureId,@Param("likeType") Integer likeType);


    /**
     * 根据用户ID和图片ID更新点赞状态
     *
     * @param userId   用户id
     * @param commentId   评论id
     * @param likeType 点赞类型
     * @return
     */
    @Select("SELECT COUNT(*) > 0 FROM user_like " +
            "WHERE user_id = #{userId} AND picture_id = #{commentId} AND like_type = #{likeType}")
    boolean isLiked(@Param("userId") Long userId, @Param("commentId") Long commentId, @Param("likeType") Integer likeType);
}