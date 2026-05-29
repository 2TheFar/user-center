package com.zhiyuan.usercenter.service;

import com.zhiyuan.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;

/**
 * 用户服务类
 *
 * @author zhiyuan
 * &#064;description  针对表【user(用户)】的数据库操作Service
 * &#064;createDate  2026-05-16 09:08:04
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount     账号
     * @param userPassword    密码
     * @param confirmPassword 确认密码
     * @param registerCode    注册码
     * @return 返回新账号的id
     */
    long userRegister(String userAccount, String userPassword, String confirmPassword, String registerCode);

    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @param request 前端请求
     * @return 返回用户信息（脱敏）
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request Web请求
     */
    void userLogout(HttpServletRequest request);

    @NonNull User getSafeUser(User user);


}

