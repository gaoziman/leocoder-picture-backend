package org.leocoder.picture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Tag;
import org.leocoder.picture.domain.dto.tag.TagRequest;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.mapper.TagMapper;
import org.leocoder.picture.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-27 16:56
 * @description :
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {


    /**
     * 通用的查询条件构造器
     *
     * @param requestParam 查询条件
     * @return LambdaQueryWrapper
     */
    @Override
    public LambdaQueryWrapper<Tag> getLambdaQueryWrapper(TagRequest requestParam) {
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        String name = requestParam.getName();
        String description = requestParam.getDescription();

        LambdaQueryWrapper<Tag> lambdaQueryWrapper = Wrappers.lambdaQuery(Tag.class);

        // 条件查询
        lambdaQueryWrapper.like(StrUtil.isNotBlank(name), Tag::getName, name);
        lambdaQueryWrapper.like(ObjUtil.isNotEmpty(description), Tag::getDescription, description);
        lambdaQueryWrapper.orderByDesc(Tag::getCreateTime);

        return lambdaQueryWrapper;
    }
}
