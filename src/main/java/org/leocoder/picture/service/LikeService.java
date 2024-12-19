package org.leocoder.picture.service;

import org.leocoder.picture.domain.Like;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description :
 */

public interface LikeService extends IService<Like> {


    /**
     * 点赞图片
     *
     * @param userId    用户id
     * @param pictureId 图片id
     * @return true表示点赞成功，false表示点赞失败
     */
    boolean likePicture(Long userId, Long pictureId);


    /**
     * 取消点赞图片
     *
     * @param userId    用户id
     * @param pictureId 图片id
     * @return true表示取消点赞成功，false表示取消点赞失败
     */
    boolean cancelLike(Long userId, Long pictureId);
}
