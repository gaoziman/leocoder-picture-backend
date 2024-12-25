package org.leocoder.picture.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.leocoder.picture.common.DeleteRequest;
import org.leocoder.picture.common.Result;
import org.leocoder.picture.common.ResultUtils;
import org.leocoder.picture.domain.Comment;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.comment.CommentAddRequest;
import org.leocoder.picture.domain.dto.comment.CommentQueryRequest;
import org.leocoder.picture.domain.dto.like.LikeRequest;
import org.leocoder.picture.domain.vo.comment.CommentVO;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.service.CommentService;
import org.leocoder.picture.service.LikeService;
import org.leocoder.picture.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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
public class CommentController {


    private final CommentService commentService;

    private final UserService userService;

    private final LikeService likeService;

    @ApiOperation(value = "分页获取评论列表")
    @PostMapping("/list/page/vo")
    public Result<IPage<CommentVO>> getCommentPage(@RequestBody CommentQueryRequest requestParam, HttpServletRequest request) {
        int current = requestParam.getPageNum();
        int pageSize = requestParam.getPageSize();
        User loginUser = userService.getLoginUser(request);
        Page<Comment> page = new Page<>(current, pageSize);
        IPage<CommentVO> commentPage = commentService.getCommentPage(page, requestParam.getPictureId(), loginUser.getId());
        return ResultUtils.success(commentPage);
    }

    @ApiOperation(value = "新增评论")
    @PostMapping("/add")
    public Result<Boolean> addComment(@RequestBody CommentAddRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 父评论ID，顶级评论为null
        Long parentId = requestParam.getParentId();
        Long pictureId = requestParam.getPictureId();
        String content = requestParam.getContent();

        // 校验参数是否完整
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureId), ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "评论内容不能为空");

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);

        // 构建评论实体
        Comment comment = new Comment();
        // 所属图片ID
        comment.setPictureId(pictureId);
        // 评论内容
        comment.setContent(content);
        // 当前登录用户ID
        comment.setUserId(loginUser.getId());
        // 父评论ID，顶级评论为null
        comment.setParentId(parentId);
        comment.setCreateTime(LocalDateTime.now());

        // 如果是子评论，校验父评论是否存在以及是否属于同一图片
        if (parentId != null) {
            // 查询父评论
            Comment parentComment = commentService.getById(parentId);
            ThrowUtils.throwIf(parentComment == null, ErrorCode.PARAMS_ERROR, "父评论不存在");
            ThrowUtils.throwIf(!parentComment.getPictureId().equals(pictureId),
                    ErrorCode.PARAMS_ERROR, "父评论与当前图片不匹配");
        }

        // 保存评论
        boolean success = commentService.save(comment);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "评论失败");

        return ResultUtils.success(true);

    }


    @ApiOperation(value = "删除评论")
    @PostMapping("/delete}")
    public Result<Boolean> deleteComment(@PathVariable DeleteRequest requestParam) {
        Long id = requestParam.getId();
        boolean result = commentService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        // 连带删除子评论
        boolean removed = commentService.remove(new QueryWrapper<Comment>().eq("parent_id", id));
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @ApiOperation(value = "点赞/取消点赞评论")
    @PostMapping("/toggleLike")
    public Result<Boolean> toggleLike(@RequestBody LikeRequest requestParam, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(requestParam), ErrorCode.PARAMS_ERROR);
        Long pictureId = requestParam.getPictureId();
        Integer likeType = requestParam.getLikeType();
        Boolean isLiked = requestParam.getIsLiked();
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 传递用户ID和评论ID
        boolean success = likeService.toggleLike(loginUser.getId(), pictureId, likeType,isLiked);
        return ResultUtils.success(success);
    }

}
