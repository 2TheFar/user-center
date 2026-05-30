package com.zhiyuan.usercenter.common;

public class ResultUtils {
    /**
     * 成功返回
     * @param data 数据
     * @return 通用返回对象
     * @param <T> 返回数据类型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败返回
     * @param errorCode 自定义异常
     * @return 通用返回对象
     * @param <T> 返回数据类型
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败返回
     * @param code 错误码
     * @param message 错误信息
     * @return 通用返回对象
     * @param <T> 返回数据类型
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }
}
