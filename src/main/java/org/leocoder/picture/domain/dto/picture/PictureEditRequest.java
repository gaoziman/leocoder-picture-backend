package org.leocoder.picture.domain.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-16 11:01
 * @description : 图片修改功能给用户使用的
 */
@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
