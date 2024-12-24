package org.leocoder.picture.domain.vo.favorite;

import lombok.Data;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-24 09:46
 * @description : 返回给前端的收藏夹信息
 */
@Data
public class FavoritePictureVO {

    /**
     * 图片id
     */
    private Long pictureId;
    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 当前用户是否收藏
     */
    private Integer isFavorited;


}
