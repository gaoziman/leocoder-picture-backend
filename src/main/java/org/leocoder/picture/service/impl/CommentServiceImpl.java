package org.leocoder.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.leocoder.picture.domain.Comment;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.vo.comment.CommentVO;
import org.leocoder.picture.mapper.CommentMapper;
import org.leocoder.picture.mapper.LikeMapper;
import org.leocoder.picture.mapper.PictureMapper;
import org.leocoder.picture.mapper.UserMapper;
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

    private final UserMapper userMapper;

    private final PictureMapper pictureMapper;


    @Override
    public IPage<CommentVO> getCommentPage(Page<Comment> page, String pictureId, Long userId) {
        // 获取图片作者 ID
        Long pictureAuthorId = getPictureAuthorId(pictureId);

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

                    // 查询用户信息
                    User user = userMapper.selectById(comment.getUserId());
                    if (user != null) {
                        commentVO.setUserName(user.getUserName());
                        commentVO.setUserAvatar(user.getUserAvatar());
                    }
                    // 设置 isAuthor 字段
                    commentVO.setAuthor(comment.getUserId().equals(pictureAuthorId));

                    // 查询是否点赞（可以通过关联 UserLike 表）
                    boolean isLiked = likeMapper.isLiked(userId, comment.getId(), 1); // 1 表示评论点赞
                    commentVO.setLiked(isLiked);

                    // 获取子评论并计算评论数量
                    List<CommentVO> subComments = getSubComments(comment.getId(), userId, pictureAuthorId);
                    commentVO.setChildren(subComments);
                    commentVO.setCommentCount(calculateCommentCount(subComments));
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
    private List<CommentVO> getSubComments(Long parentId, Long userId, Long pictureAuthorId) {
        // 查询所有直接子评论
        LambdaQueryWrapper<Comment> queryWrapper = Wrappers.lambdaQuery(Comment.class);
        queryWrapper.eq(Comment::getParentId, parentId)
                // 子评论按时间正序排列
                .orderByAsc(Comment::getCreateTime);

        List<Comment> subComments = commentMapper.selectList(queryWrapper);

        // 转换为 VO，并递归查询子评论的子评论
        return subComments.stream().map(comment -> {
            CommentVO commentVO = new CommentVO(comment);

            // 查询用户信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                commentVO.setUserName(user.getUserName());
                commentVO.setUserAvatar(user.getUserAvatar());
            }
            // 设置 isAuthor 字段
            commentVO.setAuthor(comment.getUserId().equals(pictureAuthorId));


            // 查询父评论的用户信息
            if (comment.getParentId() != null && comment.getParentId() != 0) {
                Comment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null) {
                    User parentUser = userMapper.selectById(parentComment.getUserId());
                    if (parentUser != null) {
                        commentVO.setParentUserName(parentUser.getUserName());
                    }
                }
            } else {
                commentVO.setParentUserName(null); // 当前评论是顶级评论
            }

            // 查询是否点赞（可以通过关联 UserLike 表）
            boolean isLiked = likeMapper.isLiked(userId, comment.getId(), 1); // 1 表示评论点赞
            commentVO.setLiked(isLiked);

            // 递归获取子评论
            List<CommentVO> children = getSubComments(comment.getId(), userId, pictureAuthorId);
            commentVO.setChildren(children);

            return commentVO;
        }).collect(Collectors.toList());
    }

    private Long getPictureAuthorId(String pictureId) {
        // 从数据库查询图片信息，返回图片作者的 userId
        return pictureMapper.selectAuthorIdByPictureId(pictureId);
    }

    private int calculateCommentCount(List<CommentVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return 0;
        }
        return comments.size() + comments.stream()
                .mapToInt(comment -> calculateCommentCount(comment.getChildren()))
                .sum();
    }
}
