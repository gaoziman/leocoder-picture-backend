package org.leocoder.picture.domain.vo.comment;

import lombok.Data;
import org.leocoder.picture.domain.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-19 14:41
 * @description :
 */
@Data
public class CommentVO {
    private Long id;
    private Long pictureId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    // 当前用户是否点赞该评论
    private boolean isLiked;
    // 子评论
    private List<CommentVO> children;

    public CommentVO(Comment comment) {
        this.id = comment.getId();
        this.pictureId = comment.getPictureId();
        this.userId = comment.getUserId();
        this.parentId = comment.getParentId();
        this.content = comment.getContent();
        this.likeCount = comment.getLikeCount();
        this.createTime = comment.getCreateTime();
        this.children = new ArrayList<>();
    }
}
