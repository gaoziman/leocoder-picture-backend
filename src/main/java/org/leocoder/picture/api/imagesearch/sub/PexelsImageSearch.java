package org.leocoder.picture.api.imagesearch.sub;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qcloud.cos.utils.Md5Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
@Component
public class PexelsImageSearch {

    @Value("${pexels.apiKey}")
    private String apiKey;
    private static final String ENDPOINT = "https://api.pexels.com/v1/search";

    @Value("${baidu.baiduAppId}")
    private String baiduAppId;

    @Value("${baidu.baiduApiKey}")
    private String baiduApiKey;

    private static final String BAIDU_TRANSLATE_ENDPOINT = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    private static final int DEFAULT_PAGE_SIZE = 12;

    /**
     * 使用百度翻译将中文转为英文
     *
     * @param query 中文搜索关键词
     * @return 英文翻译结果
     */
    public String translateChineseToEnglish(String query) {
        try {
            // 构建百度翻译请求参数
            String salt = String.valueOf(System.currentTimeMillis());
            String sign = baiduAppId + query + salt + baiduApiKey;
            String signMd5 = Md5Utils.md5Hex(sign);

            String url = BAIDU_TRANSLATE_ENDPOINT + "?q=" + query
                    + "&from=zh&to=en&appid=" + baiduAppId + "&salt=" + salt + "&sign=" + signMd5;

            // 发起HTTP请求
            HttpResponse response = HttpRequest.get(url).execute();
            if (response.getStatus() == 200) {
                String responseBody = response.body();
                // 使用 ObjectMapper 来解析 JSON 数据
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode resultNode = objectMapper.readTree(responseBody);
                if (resultNode != null && resultNode.has("trans_result")) {
                    return resultNode.get("trans_result").get(0).get("dst").asText();
                }
            }
        } catch (Exception e) {
            log.error("调用百度翻译 API 失败", e);
        }
        // 如果翻译失败，返回原始中文
        return query;
    }

    /**
     * 使用 Pexels API 搜索图片
     *
     * @param query 搜索关键词（英文）
     * @return 返回的图片URL列表
     */
    public List<String> searchPictures(String query, int totalCount) {
        List<String> imageUrls = new ArrayList<>();
        int page = 1;
        int fetchedImages = 0;

        while (fetchedImages < totalCount) {
            String apiUrl = String.format(
                    "%s?query=%s&per_page=%d&page=%d",
                    ENDPOINT, query, DEFAULT_PAGE_SIZE, page
            );

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .addHeader("Authorization", apiKey)
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
                        fetchedImages++;

                        // 如果已经抓取到足够的图片，跳出循环
                        if (fetchedImages >= totalCount) {
                            break;
                        }
                    }
                }

                page++; // 增加页码

                // 如果结果为空或已达到所需数量，停止请求
                if (photosNode == null || fetchedImages >= totalCount) {
                    break;
                }

            } catch (IOException e) {
                log.error("调用 Pexels API 失败", e);
                break;
            }
        }
        return imageUrls;
    }

    /**
     * 主方法：搜索中文关键词，翻译为英文并搜索图片
     */
    public  List<String> searchPicturesForChinese(String query, int totalCount) {
        String translatedQuery = translateChineseToEnglish(query);
        return searchPictures(translatedQuery, totalCount);
    }

    public static void main(String[] args) {
        // 测试：使用中文搜索并进行翻译，获取 30 张图片
        PexelsImageSearch imageSearch = new PexelsImageSearch();
        List<String> imageUrls = imageSearch.searchPicturesForChinese("狗", 18);
        System.out.println("搜索到的图片URLs: ");
        imageUrls.forEach(System.out::println);
    }
}