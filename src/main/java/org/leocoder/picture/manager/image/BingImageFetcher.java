package org.leocoder.picture.manager.image;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 14:34
 * @description : Bing图片抓取器
 */
@Slf4j
public class BingImageFetcher implements ImageFetcher {

    @Override
    public List<String> fetchImageUrls(String searchText, int page, int pageSize) {
        List<String> imageUrls = new ArrayList<>();
        String apiUrl = String.format(
                "https://www.bing.com/images/async?q=%s&first=%d&count=%d",
                searchText, (page - 1) * pageSize, pageSize
        );

        try {
            // 获取 Bing 图片搜索结果页面
            Document document = Jsoup.connect(apiUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10_000)
                    .get();

            // 提取包含图片的元素
            Elements imgElements = document.select("img.mimg");
            if (imgElements.isEmpty()) {
                log.warn("未找到 Bing 图片元素，停止抓取");
                return imageUrls;
            }

            for (Element imgElement : imgElements) {
                // 优先获取 data-src 属性，防止图片链接为空
                String dataSrc = imgElement.attr("data-src");
                String src = imgElement.attr("src");

                String fileUrl = !dataSrc.isEmpty() ? dataSrc : src;
                if (fileUrl.isEmpty()) {
                    log.info("图片链接为空，跳过该图片");
                    continue;
                }

                // 移除 URL 中的参数，保留基本的文件路径
                int questionMarkIndex = fileUrl.indexOf("?");
                if (questionMarkIndex > -1) {
                    fileUrl = fileUrl.substring(0, questionMarkIndex);
                }

                imageUrls.add(fileUrl);
            }

        } catch (Exception e) {
            log.error("Bing 图片抓取失败", e);
        }

        return imageUrls;
    }
}