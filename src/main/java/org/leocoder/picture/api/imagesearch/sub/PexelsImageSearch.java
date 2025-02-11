package org.leocoder.picture.api.imagesearch.sub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2025-02-11 13:50
 * @description : 使用 Pexels API 进行图片搜索，不指定页面和每页数量
 */
@Slf4j
public class PexelsImageSearch {

    private static final String API_KEY = "XEpGZUQnnzSbx365K0dly3NHhjC42QvvskU4W86l2lEmlSBlpGkxY6lI";
    private static final String ENDPOINT = "https://api.pexels.com/v1/search";

    // 默认的页码和每页图片数量
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 使用 Pexels API 搜索图片
     *
     * @param query 搜索关键词
     * @return 返回的图片URL列表
     */
    public List<String> searchPictures(String query) {
        return searchPictures(query, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    /**
     * 使用 Pexels API 搜索图片
     *
     * @param query 搜索关键词
     * @param page 当前页数
     * @param pageSize 每页返回的图片数量
     * @return 返回的图片URL列表
     */
    public List<String> searchPictures(String query, int page, int pageSize) {
        List<String> imageUrls = new ArrayList<>();
        String apiUrl = String.format(
                "%s?query=%s&per_page=%d&page=%d",
                ENDPOINT, query, pageSize, page
        );

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", API_KEY)
                    .build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Pexels API 调用失败，状态码：" + response.code());
            }

            String responseBody = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode photosNode = objectMapper.readTree(responseBody).get("photos");

            if (photosNode != null) {
                for (JsonNode photoNode : photosNode) {
                    // 获取每张图片的原图URL
                    imageUrls.add(photoNode.get("src").get("original").asText());
                }
            }
        } catch (IOException e) {
            log.error("调用 Pexels API 失败", e);
        }

        return imageUrls;
    }

    public static void main(String[] args) {
        // 测试不指定页面和每页数量的搜索
        PexelsImageSearch imageSearch = new PexelsImageSearch();
        List<String> imageUrls = imageSearch.searchPictures("code");
        System.out.println("搜索到的图片URLs: ");
        imageUrls.forEach(System.out::println);
    }
}