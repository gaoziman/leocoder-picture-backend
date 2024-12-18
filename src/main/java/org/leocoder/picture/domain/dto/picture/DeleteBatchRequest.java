package org.leocoder.picture.domain.dto.picture;

import lombok.Data;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 16:08
 * @description : 批量删除图片请求对象
 */
@Data
public class DeleteBatchRequest {
    private List<Long> ids;
}
