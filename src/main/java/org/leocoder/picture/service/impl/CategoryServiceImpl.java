package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Category;
import org.leocoder.picture.domain.dto.category.CategoryRequest;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.mapper.CategoryMapper;
import org.leocoder.picture.service.CategoryService;
import org.springframework.stereotype.Service;
/**
 * @author : 程序员Leo
 * @date  2024-12-27 16:56
 * @version 1.0
 * @description :
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询条件
     * @return LambdaQueryWrapper
     */
    @Override
    public LambdaQueryWrapper<Category> getLambdaQueryWrapper(CategoryRequest requestParam) {
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        String name = requestParam.getName();
        String description = requestParam.getDescription();

        LambdaQueryWrapper<Category> lambdaQueryWrapper = Wrappers.lambdaQuery(Category.class);

        // 条件查询
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Category::getName, name);
        lambdaQueryWrapper.like(ObjUtil.isNotEmpty(description), Category::getDescription, description);
        lambdaQueryWrapper.orderByDesc(Category::getCreateTime);

        return lambdaQueryWrapper;
    }
}
