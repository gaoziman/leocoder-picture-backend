package org.leocoder.picture.domain.dto.file;

import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-13 09:48
 * @description : 接受图片解析信息
 */
@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

}

