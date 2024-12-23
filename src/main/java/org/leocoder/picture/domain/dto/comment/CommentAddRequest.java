package org.leocoder.picture.domain.dto.comment;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-19 14:24
 * @description : 评论添加请求参数
 */
@Data
public class CommentAddRequest {

    private Long parentId;

    /**
     * 图片ID
     */
    private Long pictureId;


    /**
     * 用户ID
     */
    private Long userId;


    /**
     * 评论内容
     */
    private String content;
}
