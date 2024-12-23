package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.Comment;
import org.leocoder.picture.domain.vo.comment.CommentVO;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-18 21:18
 * @description :
 */

public interface CommentService extends IService<Comment> {


    /**
     * 获取评论分页信息
     *
     * @param page      分页对象
     * @param pictureId 图片id
     * @param userId    用户id
     * @return
     */
    IPage<CommentVO> getCommentPage(Page<Comment> page, String pictureId, Long userId);

}
