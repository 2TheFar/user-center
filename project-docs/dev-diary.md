# 开发随手记

这次是在一个已有 Spring Boot 用户中心项目旁边补前端。最开始先看 Controller，没有急着照计划书硬写，因为计划书里的接口只是约定，真正能不能跑还是以后端代码为准。

## 先把基础搭起来

前端选了 Vite，不用 Vue CLI。目录也按比较普通的方式拆开：

```text
frontend/src/api
frontend/src/components
frontend/src/router
frontend/src/stores
frontend/src/styles
frontend/src/types
frontend/src/views
```

登录、注册、用户管理三个页面足够覆盖当前后端能力。没有加个人中心、角色配置、菜单权限这类看起来很完整但后端并不支持的页面。

## 有些功能不能假装有

退出登录一开始就是一个例子。之前后端没有 logout 接口，前端不能装作已经把 Session 清掉了。现在后端补了 `POST /user/logout`，前端也已经接上：后端返回成功后才清理前端状态并跳回登录页。

刷新恢复登录态也是同理。现在后端已经补上 `GET /user/current`，前端启动时会用浏览器携带的 `JSESSIONID` 向后端询问当前用户，再把结果写回 Pinia。用户管理接口是否能访问，仍然以后端 Session 校验结果为准。

## 视觉上的取舍

用户中心不是活动页，也不是作品集，所以没有做大面积装饰和很跳的颜色。Ant Design Vue 本身就适合这种轻后台，主要工作是让布局舒服一些：头部导航、内容宽度、卡片边界、表格密度和移动端换行。

后面又按“即将上线的正式应用服务”方向重新收了一轮界面：顶部导航从蓝底白字改为更克制的浅色粘性导航，页面文案也从开发者说明改成普通用户能理解的产品表达。

## 联调时的判断

“无法连接后端服务”这个提示后来证明确实太泛了。命令行能访问后端，浏览器不行，那就优先怀疑跨域。Vite 代理解决之后，页面能正常拿到后端响应，剩下的提示就变成业务层面的未登录或无管理员权限。

## 已补上的登录态恢复

`GET /user/current` 已经接入。这个接口不需要前端手动告诉后端“我是谁”，而是依赖浏览器自动携带的 Session Cookie。后端根据 Cookie 找到 Session，再读取 `userLoginState` 返回当前用户。这样页面刷新后，Pinia 虽然会重新初始化，但应用启动时能重新恢复当前用户。

## 后端统一返回补完

后端现在已经补上统一响应对象和全局异常处理。接口不再混合返回 `null`、数字、布尔值和对象，而是统一返回 `BaseResponse<T>`。

这次改造后，分工更清楚：

- Controller 负责接收请求和返回成功响应。
- Service 负责业务校验和业务执行。
- 业务失败时抛 `BusinessException`。
- `GlobalExceptionHandler` 统一把异常转成错误响应。

前端下一轮联调要注意：Axios 拿到的不再是原始 `User`、`boolean` 或 `number`，而是带有 `code`、`data`、`message` 的响应对象。
