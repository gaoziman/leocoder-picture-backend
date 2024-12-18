package org.leocoder.picture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Comment;
import org.leocoder.picture.mapper.CommentMapper;
import org.leocoder.picture.service.CommentService;
import org.springframework.stereotype.Service;
/**
 * @author : 程序员Leo
 * @date  2024-12-18 21:18
 * @version 1.0
 * @description :
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService{

}
