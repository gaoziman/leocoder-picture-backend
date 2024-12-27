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
 * @date 2024-12-27 14:46
 * @description : Pexels图片抓取器
 */
@Slf4j
public class PexelsImageFetcher implements ImageFetcher {

    private static final String API_KEY = "XEpGZUQnnzSbx365K0dly3NHhjC42QvvskU4W86l2lEmlSBlpGkxY6lI";

    @Override
    public List<String> fetchImageUrls(String searchText, int page, int pageSize) {
        List<String> imageUrls = new ArrayList<>();
        String apiUrl = String.format(
                "https://api.pexels.com/v1/search?query=%s&per_page=%d&page=%d",
                searchText, pageSize, page
        );

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", API_KEY)
                    .build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Pexels API 调用失败");
            }

            String responseBody = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode photosNode = objectMapper.readTree(responseBody).get("photos");

            if (photosNode != null) {
                for (JsonNode photoNode : photosNode) {
                    imageUrls.add(photoNode.get("src").get("original").asText());
                }
            }
        } catch (Exception e) {
            log.error("Pexels 图片抓取失败", e);
        }

        return imageUrls;
    }
}
