package org.leocoder.picture.manager.image;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 14:33
 * @description : 百度图片抓取器
 */
@Slf4j
public class BaiduImageFetcher implements ImageFetcher {

    @Override
    public List<String> fetchImageUrls(String searchText, int page, int pageSize) {
        List<String> imageUrls = new ArrayList<>();
        String apiUrl = String.format("https://images.baidu.com/search/acjson?tn=resultjson_com&word=%s&pn=%d",
                searchText, (page - 1) * pageSize);

        try {
            Document document = Jsoup.connect(apiUrl).ignoreContentType(true).get();
            JsonArray imgArray = JsonParser.parseString(document.text()).getAsJsonObject().getAsJsonArray("data");

            for (JsonElement element : imgArray) {
                if (element.isJsonObject() && element.getAsJsonObject().has("thumbURL")) {
                    imageUrls.add(element.getAsJsonObject().get("thumbURL").getAsString());
                }
            }
        } catch (Exception e) {
            log.error("Baidu 图片抓取失败", e);
        }

        return imageUrls;
    }
}
