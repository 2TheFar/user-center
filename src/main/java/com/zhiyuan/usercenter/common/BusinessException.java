package com.zhiyuan.usercenter.common;

/**
 * 业务异常
 * <p>
 * 用于表示可以预期的业务错误，例如参数错误、未登录、无权限等。
 * 三种使用方式：
 * 完全依赖ErrorCode
 * code用ErrorCode，msg自定义
 * 完全自定义
 */
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final int code;

    /**
     * 使用错误码的默认提示
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        // 父类RuntimeException本身就有msg这个属性，不用重复定义，调用父类构造方法
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用错误码，并自定义提示信息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义提示信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 直接指定错误码和提示信息
     *
     * @param code    业务错误码
     * @param message 提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
