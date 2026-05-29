package com.zhiyuan.usercenter.controller;

import com.zhiyuan.usercenter.service.RegisterCodeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册码接口
 */
@RestController
@RequestMapping("/register-code")
public class RegisterCodeController {
    @Resource
    private RegisterCodeService registerCodeService;

    @Resource
    private UserController userController;

    /**
     * 管理员生成注册码
     *
     * @param request 请求
     * @return 注册码
     */
    @PostMapping("/generate")
    public String generateCode(HttpServletRequest request) {
        if (!userController.isAdmin(request)) {
            return null;
        }

        return registerCodeService.generateCode();
    }

    /**
     * 校验注册码是否可用
     *
     * @param code 注册码
     * @return 是否可用
     */
    @GetMapping("/check")
    public boolean checkCode(String code) {
        return registerCodeService.checkCode(code);
    }


}
