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
 * @description : 用户收藏表
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_favorite")
public class Favorite implements Serializable {
    /**
     * 收藏ID
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
     * 收藏时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}