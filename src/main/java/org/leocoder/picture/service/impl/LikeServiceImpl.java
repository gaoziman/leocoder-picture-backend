package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Like;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.mapper.CommentMapper;
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

    private final CommentMapper commentMapper;

    private final StringRedisTemplate redisTemplate;


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
    @Override
    public boolean toggleLike(Long userId, Long targetId, Integer likeType, boolean isLike) {
        // 构建 Redis 缓存键
        String likeCacheKey = "user:" + userId + ":liked:" + targetId + ":type:" + likeType;
        String likeCountKey = LIKE_COUNT_KEY_PREFIX + targetId + ":type:" + likeType;

        if (isLike) {
            // 点赞逻辑
            ThrowUtils.throwIf(redisTemplate.hasKey(likeCacheKey), ErrorCode.BUSINESS_ERROR, "用户已点赞");

            // 插入点赞记录
            Like userLike = new Like();
            userLike.setUserId(userId);
            // 对于评论 targetId 表示评论ID
            userLike.setPictureId(targetId);
            userLike.setLikeType(likeType);
            userLike.setIsLiked(1);
            userLikeMapper.insert(userLike);

            // 更新点赞数
            updateLikeCount(targetId, likeType, 1);

            // 设置 Redis 缓存
            redisTemplate.opsForValue().set(likeCacheKey, "1", 1, TimeUnit.DAYS);
        } else {
            // 取消点赞逻辑
            Like userLike = userLikeMapper.findByUserIdAndPictureId(userId, targetId, likeType);
            ThrowUtils.throwIf(!redisTemplate.hasKey(likeCacheKey) && ObjectUtil.isNull(userLike),
                    ErrorCode.BUSINESS_ERROR, "用户未点赞，无法取消");

            // 删除点赞记录
            userLikeMapper.deleteByUserIdAndPictureId(userId, targetId, likeType);

            // 更新点赞数
            updateLikeCount(targetId, likeType, -1);

            // 删除 Redis 缓存
            redisTemplate.delete(likeCacheKey);
        }

        return true;
    }

    /**
     * 更新点赞数
     *
     * @param targetId 点赞目标ID（图片或评论）
     * @param likeType 点赞类型（0表示图片，1表示评论）
     * @param delta    增量（1表示增加，-1表示减少）
     */
    private void updateLikeCount(Long targetId, Integer likeType, int delta) {
        // 根据类型更新数据库
        if (likeType == 0) {
            pictureMapper.updatePictureLikeCount(targetId, delta);
        } else if (likeType == 1) {
            // 如果是评论，可以实现类似 commentMapper.incrementLikeCount()
            // 这里假设有一个对应的 commentMapper
            commentMapper.updateCommentLikeCount(targetId, delta);
        }

        // 更新 Redis 缓存
        String likeCountKey = LIKE_COUNT_KEY_PREFIX + targetId + ":type:" + likeType;
        try {
            redisTemplate.opsForValue().increment(likeCountKey, delta);
        } catch (Exception e) {
            retryUpdateRedisCache(likeCountKey, delta);
        }
    }

    /**
     * 重试更新 Redis 缓存
     *
     * @param likeCountKey 点赞数缓存 key
     * @param delta        增量
     */
    private void retryUpdateRedisCache(String likeCountKey, int delta) {
        int retryCount = 0;
        boolean success = false;
        while (retryCount < 3 && !success) {
            try {
                redisTemplate.opsForValue().increment(likeCountKey, delta);
                success = true;
            } catch (Exception e) {
                retryCount++;
                if (retryCount == 3) {
                    log.error("更新 Redis 缓存失败，已尝试3次", e);
                    throw new RuntimeException("Redis 更新失败");
                }
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
