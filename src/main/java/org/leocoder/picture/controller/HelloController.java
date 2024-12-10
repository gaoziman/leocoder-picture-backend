package org.leocoder.picture.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-09 00:15
 * @description :
 */
@RestController
@Api(tags = "测试管理")
public class HelloController {


    @GetMapping("/hello")
    @ApiOperation(value = "测试接口", notes = "测试接口")
    public Result<String> hello() {
        return ResultUtils.success("hello");
    }
}
