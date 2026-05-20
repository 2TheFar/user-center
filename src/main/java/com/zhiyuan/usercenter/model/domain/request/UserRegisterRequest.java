package com.zhiyuan.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2974508926274429274L;

    // 接收账号、密码、确认密码
    private String userAccount;
    private String userPassword;
    private String confirmPassword;
}
