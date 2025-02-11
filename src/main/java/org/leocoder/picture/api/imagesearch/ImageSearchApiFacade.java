package org.leocoder.picture.api.imagesearch;

import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.api.imagesearch.model.ImageSearchResult;
import org.leocoder.picture.api.imagesearch.sub.GetImageListApi;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2025-02-11 10:03
 * @description :
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param query 图片关键字
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String query) {
        // 这里可以通过图像URL获取相关的搜索结果。因为我们使用Pexels，我们可以基于图像URL进行直接搜索。
        return GetImageListApi.getImageList(query);
    }

    public static void main(String[] args) {
        // 测试通过图像URL进行以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表：" + resultList);
    }
}
