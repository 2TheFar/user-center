package com.zhiyuan.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiyuan.usercenter.mapper.UserMapper;
import com.zhiyuan.usercenter.model.domain.User;
import com.zhiyuan.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.zhiyuan.usercenter.constant.UserConstant;

/**
 * 用户服务实现类
 *
 * @author zhiyuan
 * &#064;description  针对表【user(用户)】的数据库操作Service实现
 * &#064;createDate  2026-05-16 09:08:04
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    // 盐值，实际项目中可以放到配置文件里
    private static final String SALT = "kawaii";

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String confirmPassword) {
        // 1. 校验
        // 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, confirmPassword)) {
            return -1;
        }
        // 账号长度不小于6位
        if (userAccount.length() < 6) {
            return -2;
        }
        // 密码长度不小于8位
        if (userPassword.length() < 8 || confirmPassword.length() < 8) {
            return -3;
        }
        // 账号不能包含特殊字符
        /*
         * ^            // 从字符串开头开始匹配
         * [a-zA-Z0-9_] // 只允许英文字母、数字、下划线
         * +            // 至少出现一次
         * $            // 匹配到字符串结尾
         */
        String regex = "^[a-zA-Z0-9_]+$";
        if (!userAccount.matches(regex)) {
            return -4;
        }
        // 密码和确认密码相同
        if (!userPassword.equals(confirmPassword)) {
            return -5;
        }
        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();// 创建查询条件构造器
        queryWrapper.eq("userAccount", userAccount);// 设置查询条件：userAccount 字段等于 userAccount
        long count = this.count(queryWrapper);// 统计满足条件的用户数量
        if (count > 0) {
            return -6;
        }

        // 2. 加密
        // MD5 加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        boolean saveResult = this.save(user);
        // 实际类型是Long，返回类型是long，当保存失败时，Long的值为null，拆箱会出错，所以保存失败时不直接返回id
        if (!saveResult) {
            return -6;
        }

        // 纳尼？这个user竟然和数据库是同步的？还会反过来给user赋值的哦
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        // 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        // 账号长度不小于6位
        if (userAccount.length() < 6) {
            return null;
        }
        // 密码长度不小于8位
        if (userPassword.length() < 8) {
            return null;
        }
        // 账号不能包含特殊字符
        /*
         * ^            // 从字符串开头开始匹配
         * [a-zA-Z0-9_] // 只允许英文字母、数字、下划线
         * +            // 至少出现一次
         * $            // 匹配到字符串结尾
         */
        String regex = "^[a-zA-Z0-9_]+$";
        if (!userAccount.matches(regex)) {
            return null;
        }

        // 2. 加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 校验密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();// 创建查询条件构造器
        queryWrapper.eq("userAccount", userAccount);// 设置查询条件：userAccount 字段等于 userAccount
        queryWrapper.eq("userPassword", encryptedPassword);// 设置查询条件：userPassword 字段等于 encryptedPassword
        User user = userMapper.selectOne(queryWrapper);// 这里用userMapper的方法了，要注入，select查询
        if (user == null) {
            log.info("User login failed, userAccount cannot match userPassword.");
            return null;
        }
        // 4. 用户信息脱敏
        User safeUser = getSafeUser(user);
        // 5. 记录用户的登录态
        /*
         * 逻辑上是：
         * 把 safeUser 存到当前用户对应的 Session 里面。
         * 更准确地说，默认情况下它存到：
         * 后端服务器的 Session 区域里，也就是服务器内存中。
         */
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safeUser);

        return safeUser;
    }

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    @Override
    public @NonNull User getSafeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setGender(user.getGender());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
        return safeUser;
    }
}




