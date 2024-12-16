package org.leocoder.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.leocoder.picture.domain.User;
import org.leocoder.picture.domain.dto.user.UserQueryRequest;
import org.leocoder.picture.domain.vo.user.LoginUserVO;
import org.leocoder.picture.domain.vo.user.UserVO;
import org.leocoder.picture.enums.UserRoleEnum;
import org.leocoder.picture.exception.BusinessException;
import org.leocoder.picture.exception.ErrorCode;
import org.leocoder.picture.exception.ThrowUtils;
import org.leocoder.picture.mapper.UserMapper;
import org.leocoder.picture.service.UserService;
import org.leocoder.picture.utils.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.leocoder.picture.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-10 21:02
 * @description :
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能小于8位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }

        // 2. 检验用户是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery(User.class).eq(User::getUserAccount, userAccount);
        User existingUser = this.getOne(lambdaQueryWrapper);
        if (ObjectUtil.isNotNull(existingUser)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "用户已存在");
        }

        // 3. 密码加密
        String encryptedPassword = encryptPassword(userPassword);


        // 4.保存用户信息
        User user = User.builder().userAccount(userAccount).userPassword(encryptedPassword).userRole(UserRoleEnum.USER.getValue()).userName("无名用户").build();

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求对象
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码不能小于8位");

        // 2. 加密
        String encryptPassword = encryptPassword(userPassword);

        // 查询用户是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery(User.class).eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptPassword);
        User user = this.getOne(lambdaQueryWrapper);

        // 用户不存在
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.BUSINESS_ERROR, "用户不存在或密码错误");


        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }


    /**
     * 获取已登录用户信息
     *
     * @param request 请求对象
     * @return 已登录用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (ObjectUtil.isNull(currentUser) || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        currentUser = this.getById(currentUser.getId());
        if (ObjectUtil.isNull(currentUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return true：注销成功，false：注销失败
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (ObjectUtil.isNull(userObj)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 判断是否为管理员
     *
     * @param user 用户对象
     * @return true：是管理员，false：不是管理员
     */
    @Override
    public boolean isAdmin(User user) {
        return ObjectUtil.isNotNull(user) && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获取用户信息
     *
     * @param user 用户对象
     * @return 用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取用户信息列表
     *
     * @param userList 用户列表
     * @return 用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取用户分页信息
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户分页信息
     */
    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (ObjectUtil.isNull(userQueryRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery(User.class);
        lambdaQueryWrapper.eq(ObjUtil.isNotNull(id), User::getId, id);
        lambdaQueryWrapper.eq(ObjUtil.isNotNull(userRole), User::getUserRole, userRole);
        lambdaQueryWrapper.like(ObjUtil.isNotNull(userAccount), User::getUserAccount, userAccount);
        lambdaQueryWrapper.like(ObjUtil.isNotNull(userName), User::getUserName, userName);
        lambdaQueryWrapper.like(ObjUtil.isNotNull(userProfile), User::getUserProfile, userProfile);
        lambdaQueryWrapper.orderBy(ObjUtil.isNotNull(sortField), "ascend".equals(sortOrder), User::getId);
        return lambdaQueryWrapper;
    }


    /**
     * 对密码进行加盐加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    @Override
    public String encryptPassword(String rawPassword) {
        String salt = PasswordUtil.generateFixedSalt("leocoder");
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(salt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和盐值不能为空");
        }
        // 将密码和盐值拼接
        String saltedPassword = rawPassword + salt;
        // 使用 MD5 进行加密
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes());
    }

}
