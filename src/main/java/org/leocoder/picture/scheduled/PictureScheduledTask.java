/*
package org.leocoder.picture.scheduled;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Picture;
import org.leocoder.picture.service.PictureService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

*/
/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-25 09:58
 * @description : 定时任务 - 同步图片浏览数据
 *//*

@Slf4j
@Component
@RequiredArgsConstructor
public class PictureScheduledTask {

    private final StringRedisTemplate redisTemplate;

    private final PictureService pictureService;


    */
/**
     * 定时任务：将 Redis 中的浏览数据同步到数据库
     *//*

    @Scheduled(initialDelay = 0, fixedRate = 60000) // 任务在项目启动后立即执行一次。每隔 60s 执行一次。
    public void syncViewCountToDatabase() {
        // 获取所有以 "picture:view_count:" 开头的键
        Set<String> keys = redisTemplate.keys("picture:view_count:*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Long pictureId = Long.parseLong(key.replace("picture:view_count:", ""));
                String viewCountStr = redisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(viewCountStr)) {
                    Long viewCount = Long.parseLong(viewCountStr);
                    // 更新数据库中的浏览次数
                    Picture picture = pictureService.getById(pictureId);
                    if (ObjectUtil.isNotNull(picture)) {
                        picture.setViewCount(viewCount);
                        pictureService.updateById(picture);
                    }
                }
            }
        }
        log.info("定时任务：同步图片浏览数据到数据库完成。");
    }


    */
/**同步图片浏览数据到数据库完成
     * 定时任务：恢复 Redis 中的浏览数据
     *//*

    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void recoverViewCount() {
        // 获取所有图片ID
        List<Picture> pictureList = pictureService.list();
        List<Long> pictureIds = pictureList.stream().map(Picture::getId).toList();
        for (Long pictureId : pictureIds) {
            try {
                String key = "picture:view_count:" + pictureId;
                String redisViewCount = redisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(redisViewCount)) {
                    Long redisCount = Long.parseLong(redisViewCount);
                    Picture picture = pictureService.getById(pictureId);
                    if (picture != null && !redisCount.equals(picture.getViewCount())) {
                        // 如果 Redis 和数据库不一致，更新数据库
                        picture.setViewCount(redisCount);
                        pictureService.updateById(picture);
                    }
                }
            } catch (Exception e) {
                log.error("Error recovering view count for pictureId: {}, error: {}", pictureId, e.getMessage());
            }
        }
    }
}
*/
