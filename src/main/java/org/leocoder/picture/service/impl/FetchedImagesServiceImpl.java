package org.leocoder.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.domain.FetchedImages;
import org.leocoder.picture.mapper.FetchedImagesMapper;
import org.leocoder.picture.service.FetchedImagesService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 10:39
 * @description :
 */

@Service
@RequiredArgsConstructor
public class FetchedImagesServiceImpl extends ServiceImpl<FetchedImagesMapper, FetchedImages> implements FetchedImagesService {

    private final FetchedImagesMapper fetchedImagesMapper;

    /**
     * 获取已抓取的 URL 列表
     *
     * @param source 抓取源
     * @return 已抓取的 URL 列表
     */
    @Override
    public Set<String> getFetchedUrlsBySource(String source) {
        LambdaQueryWrapper<FetchedImages> lambdaQueryWrapper = Wrappers.lambdaQuery(FetchedImages.class)
                .eq(FetchedImages::getSource, source);
        List<FetchedImages> fetchedImagesList = fetchedImagesMapper.selectList(lambdaQueryWrapper);
        // 转换为 URL 列表
        Set<String> fetchedUrls = new HashSet<>();
        for (FetchedImages fetchedImage : fetchedImagesList) {
            fetchedUrls.add(fetchedImage.getImageUrl());
        }
        return fetchedUrls;
    }

    /**
     * 保存抓取的 URL
     *
     * @param url    图片 URL
     * @param source 抓取源
     */
    @Override
    public void saveFetchedUrl(String url, String source) {
        // 检查是否已存在
        LambdaQueryWrapper<FetchedImages> queryWrapper = Wrappers.lambdaQuery(FetchedImages.class)
                .eq(FetchedImages::getImageUrl, url);
        if (fetchedImagesMapper.selectOne(queryWrapper) != null) {
            // 已存在，直接返回或抛出异常
            return;
        }

        // 不存在则插入
        FetchedImages images = new FetchedImages();
        images.setImageUrl(url);
        images.setSource(source);
        fetchedImagesMapper.insert(images);
    }
}
