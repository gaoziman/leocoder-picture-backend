package org.leocoder.picture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.leocoder.picture.domain.Comment;

import java.util.List;

/**
 * @author : 程序员Leo
 * @date  2024-12-18 21:18
 * @version 1.0
 * @description :
 */

public interface CommentMapper extends BaseMapper<Comment> {


    @Update("UPDATE comment SET like_count = like_count + #{delta} WHERE id = #{commentId}")
    void updateCommentLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    @Select("SELECT * FROM comment WHERE picture_id = #{pictureId}")
    List<Comment> selectCommentByPictureId(String pictureId);
}