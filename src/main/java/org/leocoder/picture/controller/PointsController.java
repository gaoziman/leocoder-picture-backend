package org.leocoder.picture.controller;

import lombok.RequiredArgsConstructor;
import org.leocoder.picture.service.PointsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:21
 * @description : 用户收藏表
 */
@RestController
@RequestMapping("/user_points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService userPointsService;


}
