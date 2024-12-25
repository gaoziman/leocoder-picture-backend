package org.leocoder.picture.service;

import org.leocoder.picture.domain.Like;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description :
 */

public interface LikeService extends IService<Like> {


    /**
     * 点赞或取消点赞
     *
     * @param userId   用户 ID
     * @param targetId 点赞目标 ID（图片或评论）
     * @param likeType 点赞类型（0表示图片，1表示评论）
     * @param isLike   是否点赞
     * @return true 表示点赞成功，false 表示取消点赞成功
     */
    @Transactional
    boolean toggleLike(Long userId, Long targetId, Integer likeType, boolean isLike);
}
