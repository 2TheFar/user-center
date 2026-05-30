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
        // 如果 attribute 等于 null 或者不是 User 类型时，就返回 false；
        // 如果是，就顺便把它安全地转换成变量 user，避免手动强转出错。
        if (!(attribute instanceof User user)) {
            return false;
        }
        // 这里简化了条件分支
        return user.getUserRole() == UserConstant.ADMIN_ROLE;
    }
}
