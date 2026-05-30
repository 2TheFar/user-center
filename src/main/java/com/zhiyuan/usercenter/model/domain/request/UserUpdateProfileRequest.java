package com.zhiyuan.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户修改个人资料请求。
 */
@Data
public class UserUpdateProfileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private Integer gender;
}
