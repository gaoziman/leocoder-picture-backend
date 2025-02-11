package org.leocoder.picture.api.imagesearch;

import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.api.imagesearch.model.ImageSearchResult;
import org.leocoder.picture.api.imagesearch.sub.PexelsImageSearch;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param query 图片关键字（中文）
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String query) {
        // 先进行中文转英文
        PexelsImageSearch imageSearch = new PexelsImageSearch();
        List<String> imageUrls = imageSearch.searchPicturesForChinese(query,30);

        // 将图片的URL转为ImageSearchResult对象
        return convertToImageSearchResults(imageUrls);
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
        // 测试通过中文关键词进行图片搜索
        String query = "狗";
        List<ImageSearchResult> imageList = searchImage(query);
        System.out.println("搜索成功，获取到图片列表：" + imageList);
    }
}