package org.leocoder.picture.controller;

import lombok.RequiredArgsConstructor;
import org.leocoder.picture.service.UserLikeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description : 用户点赞控制器
 */
@RestController
@RequestMapping("/user_like")
@RequiredArgsConstructor
public class UserLikeController {

    private final UserLikeService userLikeService;

}
