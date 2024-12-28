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
import org.leocoder.picture.domain.Tag;
import org.leocoder.picture.domain.dto.tag.TagRequest;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 23:29
 * @description ： 标签管理控制器
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "标签管理")
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;


    @PostMapping("get/page/list")
    @ApiOperation(value = "分页获取标签列表")
    public Result<Page<Tag>> listTagsByPage(@RequestBody TagRequest requestParam) {
        long pageNum = requestParam.getPageNum();
        long pageSize = requestParam.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);

        Page<Tag> tagPage = tagService.page(new Page<>(pageNum, pageSize),
                tagService.getLambdaQueryWrapper(requestParam));

        return ResultUtils.success(tagPage);
    }

    @PostMapping("get/list")
    @ApiOperation(value = "获取标签列表")
    public Result<List<Tag>> listTags() {
        return ResultUtils.success(tagService.list());
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "新增标签")
    public Result<Boolean> addTag(@RequestBody TagRequest requestParam) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        String name = requestParam.getName();
        List<String> tags = requestParam.getTags();

        if (name != null && ObjectUtil.isEmpty(tags)) {
            // 查询标签是否存在
            LambdaQueryWrapper<Tag> lambdaQueryWrapper = Wrappers.lambdaQuery(Tag.class).eq(Tag::getName, requestParam.getName());
            Tag oldTag = tagService.getOne(lambdaQueryWrapper);
            ThrowUtils.throwIf(ObjectUtil.isNotNull(oldTag), ErrorCode.EXIST, "标签已存在");
            Tag tag = new Tag();
            BeanUtils.copyProperties(requestParam, tag);
            boolean result = tagService.save(tag);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        } else {
            List<Tag> tagList = new ArrayList<>();
            if (ObjectUtil.isNotNull(tags)) {
                for (String tagName : tags) {
                    // 如果不存在，则新增
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    tagList.add(tag);
                }
                boolean result = tagService.saveBatch(tagList);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            }
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改标签")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> updateTag(@RequestBody TagRequest requestParam) {
        // 校验参数
        if (ObjectUtil.isNull(requestParam) || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询标签是否存在
        LambdaQueryWrapper<Tag> lambdaQueryWrapper = Wrappers.lambdaQuery(Tag.class)
                .eq(Tag::getName, requestParam.getName())
                // 排除当前 ID;
                .ne(Tag::getId, requestParam.getId());
        Tag oldTag = tagService.getOne(lambdaQueryWrapper);
        ThrowUtils.throwIf(ObjectUtil.isNotNull(oldTag), ErrorCode.EXIST, "标签已存在");
        Tag tag = new Tag();
        BeanUtils.copyProperties(requestParam, tag);
        boolean result = tagService.updateById(tag);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @PostMapping("/delete")
    @ApiOperation(value = "删除标签")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Result<Boolean> deleteTag(@RequestBody DeleteRequest requestParam) {
        // 校验参数
        if (requestParam == null || requestParam.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = requestParam.getId();
        // 操作数据库
        boolean result = tagService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

}
