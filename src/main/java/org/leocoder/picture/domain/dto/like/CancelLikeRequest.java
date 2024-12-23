package org.leocoder.picture.domain.dto.like;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-19 09:56
 * @description : 取消点赞请求参数
 */
@Data
public class CancelLikeRequest {
    private Integer id;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 取消点赞的类型
     */
    private Integer likeType;
}
