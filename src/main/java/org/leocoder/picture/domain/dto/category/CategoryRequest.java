package org.leocoder.picture.domain.dto.category;

import lombok.Data;
import org.leocoder.picture.common.PageRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 21:25
 * @description : 分类请求参数
 */
@Data
public class CategoryRequest  extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;


    /**
     * 分类描述
     */
    private String description;
}
