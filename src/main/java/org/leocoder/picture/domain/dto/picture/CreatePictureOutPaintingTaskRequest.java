package org.leocoder.picture.domain.dto.picture;

import lombok.Data;
import org.leocoder.picture.api.aliyunai.CreateOutPaintingTaskRequest;

import java.io.Serializable;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2025-02-13 10:21
 * @description : AI 扩图请求类，用于接受前端传来的参数并传递给 Service 服务层
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}

