package org.leocoder.picture.manager.image;

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
 * @date 2024-12-27 14:30
 * @description : Google图片抓取器
 */
@Slf4j
public class GoogleImageFetcher implements ImageFetcher {

    private static final String API_KEY = "AIzaSyBvU3jEggN8AtpNcF14jyty69rFvhLlQWA";
    private static final String CX = "f112f91a23b674acc";

    @Override
    public List<String> fetchImageUrls(String searchText, int page, int pageSize) {
        List<String> imageUrls = new ArrayList<>();
        String apiUrl = String.format(
                "https://www.googleapis.com/customsearch/v1?q=%s&num=%d&searchType=image&key=%s&cx=%s&start=%d",
                searchText, pageSize, API_KEY, CX, (page - 1) * pageSize + 1
        );

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiUrl).build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Google API 调用失败");
            }

            String responseBody = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode items = objectMapper.readTree(responseBody).get("items");

            if (items != null) {
                for (JsonNode item : items) {
                    imageUrls.add(item.get("link").asText());
                }
            }
        } catch (Exception e) {
            log.error("Google 图片抓取失败", e);
        }

        return imageUrls;
    }
}
