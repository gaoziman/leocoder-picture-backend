package org.leocoder.picture.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.FetchedImages;

import java.util.Set;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 10:39
 * @description :
 */

public interface FetchedImagesService extends IService<FetchedImages> {

    /**
     * 获取已抓取的 URL 列表
     *
     * @param source 抓取源
     * @return 已抓取的 URL 列表
     */
    Set<String> getFetchedUrlsBySource(String source);

    /**
     * 保存抓取的 URL
     *
     * @param url    图片 URL
     * @param source 抓取源
     */
    void saveFetchedUrl(String url, String source);
}
