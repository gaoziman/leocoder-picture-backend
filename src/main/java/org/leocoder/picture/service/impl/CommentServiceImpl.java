package org.leocoder.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Comment;
import org.leocoder.picture.domain.vo.comment.CommentVO;
import org.leocoder.picture.mapper.CommentMapper;
import org.leocoder.picture.mapper.LikeMapper;
import org.leocoder.picture.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:18
 * @description :
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;

    private final LikeMapper likeMapper;


    @Override
    public IPage<CommentVO> getCommentPage(Page<Comment> page, String pictureId, Long userId) {
        // 查询顶级评论（parent_id 为 NULL 或 0）
        LambdaQueryWrapper<Comment> queryWrapper = Wrappers.lambdaQuery(Comment.class);
        queryWrapper.eq(Comment::getPictureId, pictureId)
                // 或者 .eq(Comment::getParentId, 0)
                .isNull(Comment::getParentId)
                // 按创建时间倒序排列
                .orderByDesc(Comment::getCreateTime);


        // 分页查询顶级评论
        IPage<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);

        // 将查询结果封装为 VO，并递归查询子评论
        List<CommentVO> commentVOList = commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO(comment);
                    // 查询子评论
                    List<CommentVO> subComments = getSubComments(comment.getId(), userId);
                    commentVO.setChildren(subComments);
                    return commentVO;
                }).collect(Collectors.toList());

        // 构建返回的分页对象
        IPage<CommentVO> resultPage = new Page<>();
        resultPage.setRecords(commentVOList);
        resultPage.setTotal(commentPage.getTotal());
        resultPage.setCurrent(commentPage.getCurrent());
        resultPage.setSize(commentPage.getSize());

        return resultPage;
    }


    /**
     * 查询子评论（递归方式）
     */
    private List<CommentVO> getSubComments(Long parentId, Long userId) {
        // 查询所有直接子评论
        LambdaQueryWrapper<Comment> queryWrapper = Wrappers.lambdaQuery(Comment.class);
        queryWrapper.eq(Comment::getParentId, parentId)
                // 子评论按时间正序排列
                .orderByAsc(Comment::getCreateTime);


        List<Comment> subComments = commentMapper.selectList(queryWrapper);

        // 转换为 VO，并递归查询子评论的子评论
        return subComments.stream().map(comment -> {
            CommentVO commentVO = new CommentVO(comment);
            // 查询是否点赞（可以通过关联 UserLike 表）
            boolean isLiked = likeMapper.isLiked(userId, comment.getId(), 1); // 1 表示评论点赞
            commentVO.setLiked(isLiked);

            // 递归获取子评论
            List<CommentVO> children = getSubComments(comment.getId(), userId);
            commentVO.setChildren(children);

            return commentVO;
        }).collect(Collectors.toList());
    }


}
