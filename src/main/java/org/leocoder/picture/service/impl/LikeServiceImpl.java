package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Like;
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

        // 使用 Redis 分布式锁，防止高并发问题
        String lockKey = "lock:" + likeCacheKey;
        boolean isLockAcquired = false;

        try {
            // 尝试获取锁
            isLockAcquired = acquireLock(lockKey, 5);

            if (isLockAcquired) {
                // 如果成功获取锁，按正常逻辑处理
                if (isLike) {
                    handleLikeWithFallback(userId, targetId, likeType, likeCacheKey, likeCountKey);
                } else {
                    handleUnlikeWithFallback(userId, targetId, likeType, likeCacheKey, likeCountKey);
                }
            } else {
                // Redis 锁不可用，降级执行
                log.warn("Redis 锁不可用，执行降级逻辑");
                if (isLike) {
                    handleLikeWithDatabaseOnly(userId, targetId, likeType);
                } else {
                    handleUnlikeWithDatabaseOnly(userId, targetId, likeType);
                }
            }
        } finally {
            // 如果获取了锁，则释放锁
            if (isLockAcquired) {
                releaseLock(lockKey);
            }
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

    /**
     * 点赞逻辑降级处理（仅操作数据库）
     */
    private void handleLikeWithDatabaseOnly(Long userId, Long targetId, Integer likeType) {
        // 检查用户是否已经点赞（从数据库中检查）
        Like existingLike = userLikeMapper.findByUserIdAndPictureId(userId, targetId, likeType);
        if (ObjectUtil.isNotNull(existingLike) && existingLike.getIsLiked() == 1) {
            throw new RuntimeException("用户已点赞");
        }

        // 插入点赞记录到数据库
        Like userLike = new Like();
        userLike.setUserId(userId);
        userLike.setPictureId(targetId);
        userLike.setLikeType(likeType);
        userLike.setIsLiked(1);
        userLikeMapper.insert(userLike);

        // 更新数据库中的点赞数
        updateLikeCount(targetId, likeType, 1);
    }

    /**
     * 取消点赞逻辑降级处理（仅操作数据库）
     */
    private void handleUnlikeWithDatabaseOnly(Long userId, Long targetId, Integer likeType) {
        // 检查用户是否未点赞（从数据库中检查）
        Like existingLike = userLikeMapper.findByUserIdAndPictureId(userId, targetId, likeType);
        if (ObjectUtil.isNull(existingLike) || existingLike.getIsLiked() == 0) {
            throw new RuntimeException("用户未点赞，无法取消");
        }

        // 删除点赞记录
        userLikeMapper.deleteByUserIdAndPictureId(userId, targetId, likeType);

        // 更新数据库中的点赞数
        updateLikeCount(targetId, likeType, -1);
    }

    /**
     * 点赞逻辑（带有 Redis 和数据库的降级支持）
     */
    private void handleLikeWithFallback(Long userId, Long targetId, Integer likeType, String likeCacheKey, String likeCountKey) {
        try {
            // 检查用户是否已经点赞
            if (Boolean.TRUE.equals(redisTemplate.hasKey(likeCacheKey))) {
                throw new RuntimeException("用户已点赞");
            }

            // 插入点赞记录
            Like userLike = new Like();
            userLike.setUserId(userId);
            userLike.setPictureId(targetId);
            userLike.setLikeType(likeType);
            userLike.setIsLiked(1);
            userLikeMapper.insert(userLike);

            // 更新点赞数
            updateLikeCount(targetId, likeType, 1);

            // 设置 Redis 缓存
            redisTemplate.opsForValue().set(likeCacheKey, "1", 1, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 不可用，降级处理点赞逻辑", e);
            handleLikeWithDatabaseOnly(userId, targetId, likeType);
        }
    }

    /**
     * 取消点赞逻辑（带有 Redis 和数据库的降级支持）
     */
    private void handleUnlikeWithFallback(Long userId, Long targetId, Integer likeType, String likeCacheKey, String likeCountKey) {
        try {
            // 检查用户是否未点赞
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(likeCacheKey))) {
                Like existingLike = userLikeMapper.findByUserIdAndPictureId(userId, targetId, likeType);
                if (ObjectUtil.isNull(existingLike)) {
                    throw new RuntimeException("用户未点赞，无法取消");
                }
            }

            // 删除点赞记录
            userLikeMapper.deleteByUserIdAndPictureId(userId, targetId, likeType);

            // 更新点赞数
            updateLikeCount(targetId, likeType, -1);

            // 删除 Redis 缓存
            redisTemplate.delete(likeCacheKey);
        } catch (Exception e) {
            log.warn("Redis 不可用，降级处理取消点赞逻辑", e);
            handleUnlikeWithDatabaseOnly(userId, targetId, likeType);
        }
    }

    /**
     * 获取 Redis 锁
     *
     * @param lockKey 锁的键
     * @param timeout 锁的过期时间（秒）
     * @return 是否成功获取锁
     */
    private boolean acquireLock(String lockKey, int timeout) {
        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(isLocked);
    }

    /**
     * 释放 Redis 锁
     *
     * @param lockKey 锁的键
     */
    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}
