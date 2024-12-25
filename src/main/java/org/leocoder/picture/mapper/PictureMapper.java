package org.leocoder.picture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.leocoder.picture.domain.Picture;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-12 23:10
 * @description :
 */
public interface PictureMapper extends BaseMapper<Picture> {


    /**
     * 增加图片的点赞数
     *
     * @param pictureId 图片id
     */
    @Update("UPDATE picture SET like_count = like_count + 1 WHERE id = #{pictureId}")
    void incrementLikeCount(@Param("pictureId") Long pictureId);

    /**
     * 减少图片的点赞数
     *
     * @param pictureId 图片id
     */
    @Update("UPDATE picture SET like_count = like_count - 1 WHERE id = #{pictureId}")
    void decrementLikeCount(@Param("pictureId") Long pictureId);


    /**
     * 增加图片的收藏数
     *
     * @param pictureId 图片id
     */
    @Update("UPDATE picture SET favorite_count = favorite_count + 1 WHERE id = #{pictureId}")
    void incrementFavoriteCount(Long pictureId);


    /**
     * 取消图片的收藏
     *
     * @param pictureId 图片id
     */
    @Update("UPDATE picture SET favorite_count = favorite_count - 1 WHERE id = #{pictureId}")
    void decrementFavoriteCount(@Param("pictureId") Long pictureId);

    @Update("UPDATE picture SET like_count = like_count + #{delta} WHERE id = #{pictureId}")
    void updatePictureLikeCount(@Param("pictureId") Long pictureId, @Param("delta") int delta);

    @Select("SELECT user_id FROM picture WHERE id = #{pictureId}")
    Long selectAuthorIdByPictureId(@Param("pictureId")String pictureId);


}