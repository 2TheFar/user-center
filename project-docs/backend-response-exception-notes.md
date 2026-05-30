# 后端统一返回与异常处理记录

本次后端改造把接口返回方式统一成 `BaseResponse<T>`，并引入业务错误码、业务异常和全局异常处理器。目标是让 Controller、Service 和异常处理各自职责更清楚。

## 核心结构

### BaseResponse

`BaseResponse<T>` 是接口统一返回对象，包含：

| 字段 | 说明 |
| --- | --- |
| `code` | 业务状态码，`0` 表示成功 |
| `data` | 真正返回给前端的数据 |
| `message` | 给前端或用户看的提示信息 |

成功示例：

```json
{
  "code": 0,
  "data": 1,
  "message": "ok"
}
```

失败示例：

```json
{
  "code": 40000,
  "data": null,
  "message": "请求参数错误"
}
```

### ErrorCode

`ErrorCode` 统一维护业务错误码。当前保留入门阶段够用的几类：

- `SUCCESS`：请求成功。
- `PARAMS_ERROR`：请求参数错误。
- `REGISTER_CODE_ERROR`：注册码格式错误、不存在、已使用或不可用。
- `NOT_LOGIN_ERROR`：用户未登录。
- `NO_AUTH_ERROR`：当前用户无权限。
- `NOT_FOUND_ERROR`：请求数据不存在。
- `SYSTEM_ERROR`：系统内部异常。

`description` 只用于开发者理解错误码，不放进 `BaseResponse` 返回给前端。

### BusinessException

`BusinessException` 表示可预期的业务失败，例如参数错误、无权限、注册码不可用。Service 中遇到业务失败时直接抛出它：

```java
throw new BusinessException(ErrorCode.REGISTER_CODE_ERROR, "注册码不可用");
```

这样 Service 只表达业务成功或失败，不关心最终 JSON 响应格式。

### GlobalExceptionHandler

`GlobalExceptionHandler` 使用 `@RestControllerAdvice` 统一拦截异常：

- `BusinessException`：返回异常里的业务错误码和提示。
- `RuntimeException`：记录日志，统一返回 `SYSTEM_ERROR`，避免把后端异常细节直接暴露给前端。

## 当前分层约定

Controller 层：

```java
public BaseResponse<T> api(...) {
    T result = service.xxx(...);
    return ResultUtils.success(result);
}
```

Service 层：

```java
if (参数不合法) {
    throw new BusinessException(ErrorCode.PARAMS_ERROR);
}

return 业务结果;
```

全局异常处理器：

```java
BusinessException -> BaseResponse(code, null, message)
RuntimeException -> BaseResponse(SYSTEM_ERROR)
```

## 当前接口返回格式

用户和注册码相关接口现在都返回 `BaseResponse<T>`：

- `POST /user/register`：`BaseResponse<Long>`
- `POST /user/login`：`BaseResponse<User>`
- `POST /user/logout`：`BaseResponse<Boolean>`
- `GET /user/search`：`BaseResponse<List<User>>`
- `POST /user/delete`：`BaseResponse<Boolean>`
- `POST /register-code/generate`：`BaseResponse<String>`
- `GET /register-code/check`：`BaseResponse<Boolean>`

前端后续需要统一按 `code === 0` 判断成功，再读取 `data`。
