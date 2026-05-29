package com.zhiyuan.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhiyuan.usercenter.model.domain.RegisterCode;

public interface RegisterCodeService extends IService<RegisterCode> {
    /**
     * 生成注册码
     *
     * @return 注册码
     */
    String generateCode();

    /**
     * 校验注册码是否可用
     *
     * @param code 注册码
     * @return 可用返回true不可用返回false
     */
    boolean checkCode(String code);

    /**
     * 使用注册码
     *
     * @param code 注册码
     * @param userId 使用者用户 id
     * @return 是否使用成功
     */
    boolean useCode(String code,Long userId);
}
