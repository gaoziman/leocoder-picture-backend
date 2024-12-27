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
 * @date 2024-12-27 14:45
 * @description : Wallhaven图片抓取器
 */
@Slf4j
public class WallhavenImageFetcher implements ImageFetcher {

    private static final String API_KEY = "JaROJlCiPGYkyNWzAkx9vLQO5aojhDFP";

    @Override
    public List<String> fetchImageUrls(String searchText, int page, int pageSize) {
        List<String> imageUrls = new ArrayList<>();
        String apiUrl = String.format(
                "https://wallhaven.cc/api/v1/search?q=%s&page=%d&apikey=%s",
                searchText, page, API_KEY
        );

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiUrl).build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Wallhaven API 调用失败");
            }

            String responseBody = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode dataNode = objectMapper.readTree(responseBody).get("data");

            if (dataNode != null) {
                for (JsonNode imageNode : dataNode) {
                    imageUrls.add(imageNode.get("path").asText());
                }
            }
        } catch (Exception e) {
            log.error("Wallhaven 图片抓取失败", e);
        }

        return imageUrls;
    }
}