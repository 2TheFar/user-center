package com.zhiyuan.usercenter.common;

import com.zhiyuan.usercenter.constant.UserConstant;
import com.zhiyuan.usercenter.model.domain.User;
import jakarta.servlet.http.HttpServletRequest;

public class UserUtils {
    /**
     * 是否为管理员
     *
     * @param request 网络请求
     * @return 是就true不是就false
     */
    public static boolean isAdmin(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (!(attribute instanceof User user)) {
            return false;
        }
        // 这里简化了条件分支
        return user.getUserRole() == UserConstant.ADMIN_ROLE;
    }
}
