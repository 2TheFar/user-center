package com.zhiyuan.usercenter.controller;

import com.zhiyuan.usercenter.common.BaseResponse;
import com.zhiyuan.usercenter.common.BusinessException;
import com.zhiyuan.usercenter.common.ErrorCode;
import com.zhiyuan.usercenter.common.ResultUtils;
import com.zhiyuan.usercenter.service.RegisterCodeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.zhiyuan.usercenter.common.UserUtils.isAdmin;

/**
 * 注册码接口
 */
@RestController
@RequestMapping("/register-code")
public class RegisterCodeController {
    @Resource
    private RegisterCodeService registerCodeService;


    /**
     * 管理员生成注册码
     *
     * @param request 请求
     * @return 注册码
     */
    @PostMapping("/generate")
    public BaseResponse<String> generateCode(HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        String code = registerCodeService.generateCode();
        return ResultUtils.success(code);
    }

    /**
     * 校验注册码是否可用
     *
     * @param code 注册码
     * @return 是否可用
     */
    @GetMapping("/check")
    public BaseResponse<Boolean> checkCode(String code) {
        boolean result = registerCodeService.checkCode(code);
        return ResultUtils.success(result);
    }


}
