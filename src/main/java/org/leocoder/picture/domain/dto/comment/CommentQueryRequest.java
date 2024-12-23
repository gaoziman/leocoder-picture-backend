package org.leocoder.picture.domain.dto.comment;

import lombok.Data;
import org.leocoder.picture.common.PageRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-19 14:29
 * @description :
 */
@Data
public class CommentQueryRequest extends PageRequest {
    private String id;


    /**
     * 用户id
     */
    private String userId;


    /**
     * 图片id
     */
    private String pictureId;

}
