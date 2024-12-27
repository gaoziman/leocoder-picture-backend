package org.leocoder.picture.manager.image;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 14:29
 * @description : 定义统一的抓取接口
 */
public interface ImageFetcher {
    /**
     * 获取当前数据源的图片列表
     *
     * @param searchText 搜索关键词
     * @param page       页码
     * @param pageSize   每页大小
     * @return 图片 URL 列表
     */
    List<String> fetchImageUrls(String searchText, int page, int pageSize);
}