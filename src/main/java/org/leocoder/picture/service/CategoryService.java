package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.Category;
import org.leocoder.picture.domain.dto.category.CategoryRequest;

/**
 * @author : 程序员Leo
 * @date  2024-12-27 16:56
 * @version 1.0
 * @description :
 */

public interface CategoryService extends IService<Category>{

    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询条件
     * @return LambdaQueryWrapper
     */
    LambdaQueryWrapper<Category> getLambdaQueryWrapper(CategoryRequest requestParam);
}
