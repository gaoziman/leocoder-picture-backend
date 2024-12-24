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
 * @date  2024-12-12 23:10
 * @version 1.0
 * @description : 图片实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "picture")
public class Picture implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片 url
     */
    @TableField(value = "url")
    private String url;

    /**
     * 图片名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 简介
     */
    @TableField(value = "introduction")
    private String introduction;

    /**
     * 分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 图片体积
     */
    @TableField(value = "pic_size")
    private Long picSize;

    /**
     * 图片宽度
     */
    @TableField(value = "pic_width")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @TableField(value = "pic_height")
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @TableField(value = "pic_scale")
    private Double picScale;

    /**
     * 图片格式
     */
    @TableField(value = "pic_format")
    private String picFormat;

    /**
     * 创建用户 id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 审核状态
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     *  审核内容
     */
    @TableField(value = "review_message")
    private String reviewMessage;

    /**
     * 审核人 id
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    @TableField(value = "edit_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime editTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "like_count")
    private Integer likeCount;


    /**
     * 收藏数量
     */
    @TableField(value = "favorite_count")
    private Integer favoriteCount;

    /**
     * 是否删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}