package org.leocoder.picture.domain.dto.favorite;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-23 16:55
 * @description : 收藏请求对象
 */
@Data
public class FavoriteRequest {
    private Long userId;

    private Long pictureId;
}
