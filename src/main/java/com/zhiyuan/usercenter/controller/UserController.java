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
import com.zhiyuan.usercenter.model.domain.request.UserUpdateProfileRequest;
import com.zhiyuan.usercenter.service.AvatarStorageService;
import com.zhiyuan.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zhiyuan.usercenter.common.UserUtils.isAdmin;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private AvatarStorageService avatarStorageService;

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

    @PostMapping("/profile/update")
    public BaseResponse<User> updateCurrentUserProfile(@RequestBody UserUpdateProfileRequest updateProfileRequest,
                                                       HttpServletRequest request) {
        if (updateProfileRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userProfile = new User();
        userProfile.setUsername(updateProfileRequest.getUsername());
        userProfile.setAvatarUrl(updateProfileRequest.getAvatarUrl());
        userProfile.setPhone(updateProfileRequest.getPhone());
        userProfile.setEmail(updateProfileRequest.getEmail());
        userProfile.setGender(updateProfileRequest.getGender());

        User updatedUser = userService.updateCurrentUserProfile(userProfile, request);
        return ResultUtils.success(updatedUser);
    }

    /**
     * 上传当前用户头像
     * （当用户确定保存资料时才上传最终版的头像，上传成功后立即把返回的头像路径随资料一起保存。）
     *
     * @param avatar  头像文件，必须是图片格式
     * @param request HTTP请求对象，用于验证用户登录状态
     * @return 返回上传后的头像访问URL
     */
    /*
     * consumes表示这个接口接收的是：文件上传表单格式。
     * 也就是请求头里一般是：Content-Type: multipart/form-data
     * 上传文件时不能用普通 JSON，而要用 FormData。
     */
    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // 表示从请求体里拿出名叫 avatar 的文件。MultipartFile avatar表示上传过来的头像文件。
    public BaseResponse<String> uploadCurrentUserAvatar(@RequestPart("avatar") MultipartFile avatar,
                                                        HttpServletRequest request) {
        assertLoggedIn(request);
        String avatarUrl = avatarStorageService.saveAvatar(avatar);
        return ResultUtils.success(avatarUrl);
    }

    /**
     * 获取用户头像
     * 根据文件名从存储系统中加载头像资源，并设置合适的Content-Type和缓存策略（缓存30天）。
     *
     * @param fileName 头像文件名，包含文件扩展名
     * @return 返回包含头像资源的HTTP响应实体，包含内容类型和缓存控制头
     */
    /*
     * 表示把路径里的{文件名}提取出来，赋值给参数fileName。
     * 这里的 :.+ 很重要，意思是：文件名可以包含点号 .
     */
    @GetMapping("/avatar/{fileName:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getAvatar(@PathVariable String fileName) {
        org.springframework.core.io.Resource avatarResource = avatarStorageService.loadAvatar(fileName);
        /*
         * 构建一个 HTTP 200 成功响应。
         * 设置响应内容类型。
         * 设置浏览器缓存。
         * 把头像文件资源放进响应体里返回。
         * 最终浏览器拿到的就是图片文件内容。
         */
        return ResponseEntity.ok()
                .contentType(getAvatarMediaType(fileName))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(avatarResource);
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

    /**
     * 验证用户是否已登录
     * 检查HTTP请求中的会话是否存在，以及会话中是否包含有效的用户登录状态信息。
     * 如果未登录或参数无效，将抛出相应的业务异常。
     *
     * @param request HTTP请求对象，用于获取会话信息和用户登录状态
     */
    private void assertLoggedIn(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(UserConstant.USER_LOGIN_STATE) instanceof User)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    /**
     * 获取图片类型
     * 根据头像文件名的后缀，判断它应该返回什么图片类型 MediaType。
     *
     * @param fileName 文件名
     * @return 图片类型
     */
    private MediaType getAvatarMediaType(String fileName) {
        // 把文件名统一转成小写。
        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lowerFileName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lowerFileName.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_PNG;
    }
}
