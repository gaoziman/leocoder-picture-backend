package org.leocoder.picture.api.imagesearch;

import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.api.imagesearch.model.ImageSearchResult;
import org.leocoder.picture.api.imagesearch.sub.GetImageFirstUrlApi;
import org.leocoder.picture.api.imagesearch.sub.GetImageListApi;
import org.leocoder.picture.api.imagesearch.sub.GetImagePageUrlApi;

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
     * @param imageUrl 图片地址
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}

