package com.zhiyuan.usercenter.common;

public enum ErrorCode {

    /**
     * 成功
     */
    SUCCESS(0, "ok", "请求成功"),

    /**
     * 请求参数错误，例如参数为空、格式不正确、长度不合法等
     */
    PARAMS_ERROR(40000, "请求参数错误", "传入的参数不符合要求"),

    /**
     * 注册码错误，例如注册码格式错误、不存在或已被使用
     */
    REGISTER_CODE_ERROR(40001, "注册码错误", "注册码格式不正确或当前不可用"),

    /**
     * 用户未登录，需要先登录后才能访问
     */
    NOT_LOGIN_ERROR(40100, "未登录", "用户还没有登录"),

    /**
     * 用户无权限，例如普通用户访问管理员接口
     */
    NO_AUTH_ERROR(40101, "无权限", "当前用户没有操作权限"),

    /**
     * 请求的数据不存在，例如查询不存在的用户
     */
    NOT_FOUND_ERROR(40400, "请求数据不存在", "要操作的数据不存在"),

    /**
     * 系统内部异常，通常表示后端代码或服务器出现未知问题
     */
    SYSTEM_ERROR(50000, "系统内部异常", "服务器处理请求时出现异常");

    /**
     * 业务状态码
     */
    private final int code;

    /**
     * 返回给前端或用户看的简短提示
     */
    private final String message;

    /**
     * 给开发者看的说明，方便理解这个错误码什么时候使用
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    /*
     * 枚举类型不支持@Data
     * 也不支持set
     */
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
