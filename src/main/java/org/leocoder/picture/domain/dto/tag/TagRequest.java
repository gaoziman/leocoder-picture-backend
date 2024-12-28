package org.leocoder.picture.domain.dto.tag;

import lombok.Data;
import org.leocoder.picture.common.PageRequest;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 21:25
 * @description :
 */
@Data
public class TagRequest  extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签
     */
    private List<String> tags;
}
