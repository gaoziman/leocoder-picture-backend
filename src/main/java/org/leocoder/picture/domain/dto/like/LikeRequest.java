package org.leocoder.picture.domain.dto.like;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-19 09:56
 * @description : 点赞请求对象
 */
@Data
public class LikeRequest {
    private Integer id;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 点赞类型
     */
    private Integer likeType;
}
