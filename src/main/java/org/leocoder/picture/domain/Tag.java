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
 * @date  2024-12-27 16:56
 * @version 1.0
 * @description : 标签实体类
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tag")
public class Tag implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标签名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 标签描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}