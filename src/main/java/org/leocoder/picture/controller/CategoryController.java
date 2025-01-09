package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.annotation.AuthCheck;
import org.leocoder.picture.common.DeleteRequest;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.constant.UserConstant;
import org.leocoder.picture.domain.Category;
import org.leocoder.picture.domain.dto.category.CategoryRequest;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 23:29
 * @description ： 分类管理控制器
 */
@RequestMapping("/category")
@RestController
@RequiredArgsConstructor
@Api(tags = "分类管理")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("get/page/list")
    @ApiOperation(value = "分页获取分类列表")
    public Result<Page<Category>> listCategoryByPage(@RequestBody CategoryRequest requestParam) {
        long pageNum = requestParam.getPageNum();
        long pageSize = requestParam.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);

        Page<Category> categoryPage = categoryService.page(new Page<>(pageNum, pageSize),
                categoryService.getLambdaQueryWrapper(requestParam));
        return ResultUtils.success(categoryPage);
    }

    @PostMapping("get/list")
    @ApiOperation(value = "获取分类列表")
    public Result<List<Category>> listCategory() {
        return ResultUtils.success(categoryService.list());
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "新增分类")
    public Result<Boolean> addCategory(@RequestBody CategoryRequest  requestParam) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        // 查询标签是否存在
        LambdaQueryWrapper<Category> lambdaQueryWrapper = Wrappers.lambdaQuery(Category.class).eq(Category::getName, requestParam.getName());
        Category oldCategory = categoryService.getOne(lambdaQueryWrapper);
        ThrowUtils.throwIf(ObjectUtil.isNotNull(oldCategory), ErrorCode.EXIST, "分类已存在");
        Category category = new Category();
        BeanUtils.copyProperties(requestParam, category);
        boolean result = categoryService.save(category);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改分类")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> updateCategory(@RequestBody CategoryRequest  requestParam) {
        // 校验参数
        if (ObjectUtil.isNull(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询标签是否存在
        LambdaQueryWrapper<Category> lambdaQueryWrapper = Wrappers.lambdaQuery(Category.class)
                .eq(Category::getName, requestParam.getName())
                // 排除当前 ID;
                .ne(Category::getId, requestParam.getId());
        Category oldCategory = categoryService.getOne(lambdaQueryWrapper);
        ThrowUtils.throwIf(ObjectUtil.isNotNull(oldCategory), ErrorCode.EXIST, "分类已存在");
        Category category = new Category();
        BeanUtils.copyProperties(requestParam, category);
        boolean result = categoryService.updateById(category);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @PostMapping("/delete")
    @ApiOperation(value = "删除分类")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> deleteCategory(@RequestBody DeleteRequest requestParam) {
        // 校验参数
        if (requestParam == null || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = requestParam.getId();
        // 操作数据库
        boolean result = categoryService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

}
