package org.leocoder.picture.api.imagesearch.sub;

import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.api.imagesearch.model.ImageSearchResult;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2025-02-10 09:18
 * @description :
 */
@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     *
     * @param query 搜索关键词
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> getImageList(String query) {
        try {
            // 使用 Pexels API 搜索图片
            PexelsImageSearch imageSearch = new PexelsImageSearch();
            List<String> imageUrls = imageSearch.searchPictures(query);

            // 将图片的URL转为ImageSearchResult对象
            return convertToImageSearchResults(imageUrls);
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    /**
     * 将图片URL列表转换为ImageSearchResult列表
     *
     * @param imageUrls 图片URL列表
     * @return ImageSearchResult列表
     */
    private static List<ImageSearchResult> convertToImageSearchResults(List<String> imageUrls) {
        List<ImageSearchResult> imageSearchResults = new ArrayList<>();
        for (String url : imageUrls) {
            ImageSearchResult result = new ImageSearchResult();
            // 使用Pexels提供的原图URL作为缩略图
            result.setThumbUrl(url);
            // 这里假设来源地址与缩略图相同
            result.setFromUrl(url);
            imageSearchResults.add(result);
        }
        return imageSearchResults;
    }

    public static void main(String[] args) {
        // 示例：通过 Pexels API 获取与“nature”相关的图片列表
        String query = "nature";
        List<ImageSearchResult> imageList = getImageList(query);
        System.out.println("搜索成功，获取到图片列表：" + imageList);
    }
}

