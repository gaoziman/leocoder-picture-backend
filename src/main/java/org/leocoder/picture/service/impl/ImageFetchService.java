/*
package org.leocoder.picture.service.impl;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.picture.PictureUploadRequest;
import org.leocoder.picture.domain.vo.picture.PictureVO;
import org.leocoder.picture.manager.image.ImageFetcher;
import org.leocoder.picture.service.FetchedImagesService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

*/
/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 14:38
 * @description :
 *//*

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageFetchService {

    private final FetchedImagesService fetchedImagesService;

    private final PictureServiceImpl pictureService;

    */
/**
     * 通用的抓取图片逻辑
     *
     * @param fetcher    图片抓取器
     * @param searchText 搜索关键词
     * @param count      抓取数量
     * @param loginUser  登录用户
     * @param namePrefix 图片名称前缀
     * @param source     数据源名称
     * @return 成功抓取的图片数量
     *//*
成功抓取的图片数量
    public Integer fetchImages(ImageFetcher fetcher, String searchText, Integer count, User loginUser,
                               String namePrefix, String source) {
        int uploadCount = 0;
        int page = 1;
        int pageSize = 10;

        // 获取已抓取的 URL 列表
        Set<String> fetchedUrls = fetchedImagesService.getFetchedUrlsBySource(source);

        while (uploadCount < count) {
            // 获取当前页图片 URL 列表
            List<String> imageUrls = fetcher.fetchImageUrls(searchText, page, pageSize);

            if (imageUrls.isEmpty()) {
                log.warn("未找到更多图片，停止抓取");
                break;
            }

            for (String fileUrl : imageUrls) {
                if (fetchedUrls.contains(fileUrl)) {
                    log.info("图片已抓取过，跳过: {}", fileUrl);
                    continue;
                }

                // 检查文件大小
                try {
                    long fileSize = getFileSize(fileUrl);
                    if (fileSize > 4 * 1024 * 1024) {
                        log.warn("图片文件大小超过4MB，跳过: {}", fileUrl);
                        continue;
                    }
                } catch (IOException e) {
                    log.warn("无法获取文件大小，跳过: {}", fileUrl, e);
                    continue;
                }

                // 上传图片
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));

                try {
                    PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                    log.info("图片上传成功, id = {}", pictureVO.getId());
                    uploadCount++;
                    fetchedImagesService.saveFetchedUrl(fileUrl, source);
                } catch (Exception e) {
                    log.error("图片上传失败", e);
                }

                if (uploadCount >= count) {
                    break;
                }
            }

            page++;
        }

        return uploadCount;
    }


    */
/**
     * 获取文件大小
     *
     * @param fileUrl 文件 URL
     * @return 文件大小
     * @throws IOException 异常
     *//*

    private long getFileSize(String fileUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(fileUrl).head().build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("无法获取文件大小: " + fileUrl);
        }
        String contentLength = response.header("Content-Length");
        return contentLength != null ? Long.parseLong(contentLength) : 0;
    }
}
*/
