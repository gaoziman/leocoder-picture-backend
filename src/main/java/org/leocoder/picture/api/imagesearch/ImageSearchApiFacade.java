package org.leocoder.picture.api.imagesearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.api.imagesearch.model.ImageSearchResult;
import org.leocoder.picture.api.imagesearch.sub.PexelsImageSearch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2025-02-11 13:50
 * @description : 使用 Pexels API 进行图片搜索，不指定页面和每页数量
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageSearchApiFacade {

    private final PexelsImageSearch pexelsImageSearch;

    /**
     * 搜索图片
     *
     * @param query 图片关键字（中文）
     * @return 图片搜索结果列表
     */
    public List<ImageSearchResult> searchImage(String query) {
        // 先进行中文转英文
        List<String> imageUrls = pexelsImageSearch.searchPicturesForChinese(query, 18);

        // 将图片的URL转为ImageSearchResult对象
        return convertToImageSearchResults(imageUrls);
    }

    /**
     * 将图片URL列表转换为ImageSearchResult列表
     *
     * @param imageUrls 图片URL列表
     * @return ImageSearchResult列表
     */
    private List<ImageSearchResult> convertToImageSearchResults(List<String> imageUrls) {
        List<ImageSearchResult> imageSearchResults = new ArrayList<>();
        for (String url : imageUrls) {
            ImageSearchResult result = new ImageSearchResult();
            result.setThumbUrl(url);
            result.setFromUrl(url);
            imageSearchResults.add(result);
        }
        return imageSearchResults;
    }
}