package org.leocoder.picture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
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
}