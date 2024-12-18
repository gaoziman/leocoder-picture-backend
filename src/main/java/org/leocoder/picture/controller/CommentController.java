package org.leocoder.picture.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.service.CommentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:18
 * @description : 图片评论控制器
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Api(tags = "图片评论管理")
public class CommentController {ˆ


    private final CommentService commentService;


}
