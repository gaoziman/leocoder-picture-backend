package org.leocoder.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.user.UserQueryRequest;
import org.leocoder.picture.domain.vo.user.LoginUserVO;
import org.leocoder.picture.domain.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:02
 * @description :
 */

public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 获取用户信息
     *
     * @param user 用户对象
     * @return 用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户信息列表
     *
     * @param userList 用户列表
     * @return 用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取用户分页信息
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户分页信息
     */
    LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    String encryptPassword(String rawPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求对象
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request 请求对象
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return true：注销成功，false：注销失败
     */
    boolean userLogout(HttpServletRequest request);


}
