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
 * @date  2024-12-27 10:39
 * @version 1.0
 * @description : 记录已抓取的图片信息
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "fetched_images")
public class FetchedImages implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "image_url")
    private String imageUrl;

    @TableField(value = "hash_value")
    private String hashValue;

    @TableField(value = "`source`")
    private String source;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}