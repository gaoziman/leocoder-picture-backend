package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Like;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.mapper.LikeMapper;
import org.leocoder.picture.mapper.PictureMapper;
import org.leocoder.picture.service.LikeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.leocoder.picture.constant.LikeConstant.LIKE_COUNT_KEY_PREFIX;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description :
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    private final LikeMapper userLikeMapper;

    private final PictureMapper pictureMapper;

    private final StringRedisTemplate redisTemplate;


    /**
     * 点赞图片
     *
     * @param userId    用户id
     * @param pictureId 图片id
     * @return true表示点赞成功，false表示点赞失败
     */
    @Override
    @Transactional
    public boolean likePicture(Long userId, Long pictureId) {
        // 检查用户是否已经点赞
        String likeCacheKey = "user:" + userId + ":liked:" + pictureId;
        log.info("用户[{}]已经点赞过图片[{}]", userId, pictureId);
        ThrowUtils.throwIf(redisTemplate.hasKey(likeCacheKey), ErrorCode.BUSINESS_ERROR, "用户已经点赞过图片");

        // 插入点赞记录
        Like userLike = new Like();
        userLike.setUserId(userId);
        userLike.setPictureId(pictureId);
        userLike.setIsLiked(1);
        userLikeMapper.insert(userLike);

        // 更新图片的点赞数
        pictureMapper.incrementLikeCount(pictureId);

        // 设置 Redis 缓存的过期时间为 1 天，可以根据实际情况调整
        redisTemplate.opsForValue().set(likeCacheKey, "1", 1, TimeUnit.DAYS);

        // 更新 Redis 缓存，并设置过期时间
        String likeCountKey = LIKE_COUNT_KEY_PREFIX + pictureId;
        try {
            redisTemplate.opsForValue().set(likeCacheKey, "1", 1, TimeUnit.DAYS);
            redisTemplate.opsForValue().increment(likeCountKey, 1);
        } catch (Exception e) {
            // 如果 Redis 更新失败，重试机制
            retryUpdateRedisCache(likeCacheKey, likeCountKey);
        }

        return true;
    }


    /**
     * 取消点赞图片
     *
     * @param userId    用户id
     * @param pictureId 图片id
     * @return true表示取消点赞成功，false表示取消点赞失败
     */
    @Override
    @Transactional
    public boolean cancelLike(Long userId, Long pictureId) {
        // 检查用户是否点赞过
        String likeCacheKey = "user:" + userId + ":liked:" + pictureId;

        Like userLike = userLikeMapper.findByUserIdAndPictureId(userId, pictureId);

        ThrowUtils.throwIf(!redisTemplate.hasKey(likeCacheKey) && ObjectUtil.isNull(userLike), ErrorCode.BUSINESS_ERROR, " 用户未点赞，无法取消");

        // 删除点赞记录
        userLikeMapper.deleteByUserIdAndPictureId(userId, pictureId);

        // 更新图片的点赞数
        pictureMapper.decrementLikeCount(pictureId);

        // 删除 Redis 缓存中的点赞状态
        redisTemplate.delete(likeCacheKey);

        // 更新 Redis 中的点赞数
        String likeCountKey = LIKE_COUNT_KEY_PREFIX + pictureId;
        redisTemplate.opsForValue().increment(likeCountKey, -1);

        return true;
    }

    /**
     * 更新 Redis 缓存，并设置过期时间
     *
     * @param likeCacheKey 点赞缓存 key
     * @param likeCountKey 点赞数缓存 key
     */
    private void retryUpdateRedisCache(String likeCacheKey, String likeCountKey) {
        // 尝试重试，最多重试 3 次
        int retryCount = 0;
        boolean success = false;
        while (retryCount < 3 && !success) {
            try {
                // 尝试更新 Redis 缓存
                redisTemplate.opsForValue().set(likeCacheKey, "1", 1, TimeUnit.DAYS);
                redisTemplate.opsForValue().increment(likeCountKey, 1);
                success = true;
            } catch (Exception e) {
                retryCount++;
                if (retryCount == 3) {
                    log.error("更新 Redis 缓存失败，已尝试3次，执行失败", e);
                    throw new RuntimeException("Redis 更新失败");
                }
                try {
                    // 延迟重试，避免频繁操作
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
