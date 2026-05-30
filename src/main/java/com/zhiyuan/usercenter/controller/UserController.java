package com.zhiyuan.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhiyuan.usercenter.common.BaseResponse;
import com.zhiyuan.usercenter.common.BusinessException;
import com.zhiyuan.usercenter.common.ErrorCode;
import com.zhiyuan.usercenter.common.ResultUtils;
import com.zhiyuan.usercenter.constant.UserConstant;
import com.zhiyuan.usercenter.model.domain.User;
import com.zhiyuan.usercenter.model.domain.request.UserLoginRequest;
import com.zhiyuan.usercenter.model.domain.request.UserRegisterRequest;
import com.zhiyuan.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.zhiyuan.usercenter.common.UserUtils.isAdmin;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 注册接口方法
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String confirmPassword = userRegisterRequest.getConfirmPassword();
        String registerCode = userRegisterRequest.getRegisterCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, confirmPassword, registerCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, confirmPassword, registerCode);
        return ResultUtils.success(result);
    }

    // 登录接口方法
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 这个userLogin是自己写的，如果失败了在Service就中断了，反之如果到了Controller肯定是成功了
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (!(userObj instanceof User currentUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(currentUser);
    }

    // 查询用户方法
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);

        List<User> list = userList.stream().map(user -> {
            return userService.getSafeUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(list);
    }


    // 删除用户方法
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        // 如果删除失败了不应该返回success(false)，应该抛出删除失败的异常
        if (!result) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在或删除失败");
        }
        return ResultUtils.success(true);

    }

    // 退出登录方法
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.userLogout(request);
        return ResultUtils.success(true);
    }
}
