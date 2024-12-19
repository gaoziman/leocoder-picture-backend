package org.leocoder.picture.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : 程序员Leo
 * @date  2024-12-18 21:21
 * @version 1.0
 * @description : 用户点赞表
 */


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_like")
public class Like implements Serializable {
    /**
     * 点赞ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 图片ID
     */
    @TableField(value = "picture_id")
    private Long pictureId;

    /**
     * 点赞时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "is_liked")
    private Integer isLiked;

    private static final long serialVersionUID = 1L;
}