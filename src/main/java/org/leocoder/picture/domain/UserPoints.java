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
 * @description : 用户积分记录表
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_points")
public class UserPoints implements Serializable {
    /**
     * 积分ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 操作类型：like, comment, upload
     */
    @TableField(value = "`action`")
    private String action;

    /**
     * 积分值
     */
    @TableField(value = "points")
    private Integer points;

    /**
     * 操作时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}