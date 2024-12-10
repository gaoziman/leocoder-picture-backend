package org.leocoder.picture.domain.dto.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.leocoder.picture.common.PageRequest;

import java.io.Serializable;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 23:17
 * @description :
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}

