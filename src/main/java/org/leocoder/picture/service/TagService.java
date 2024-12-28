package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.Tag;
import org.leocoder.picture.domain.dto.tag.TagRequest;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 16:56
 * @description :
 */

public interface TagService extends IService<Tag> {


    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询条件
     * @return LambdaQueryWrapper
     */
    LambdaQueryWrapper<Tag> getLambdaQueryWrapper(TagRequest requestParam);
}
