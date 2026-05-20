package com.zhiyuan.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8732214119966914654L;

    //账号和密码
    private String userAccount;
    private String userPassword;
}
