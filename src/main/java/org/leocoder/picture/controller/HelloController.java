package org.leocoder.picture.controller;

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
public class HelloController {


    @GetMapping("/hello")
    public Result<String> hello() {
        return ResultUtils.success("hello");
    }
}
