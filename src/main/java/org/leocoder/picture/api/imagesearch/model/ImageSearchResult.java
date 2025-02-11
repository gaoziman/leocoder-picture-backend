package org.leocoder.picture.api.imagesearch.model;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2025-02-10 09:14
 * @description :
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}

