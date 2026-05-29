# 接口联调笔记：看起来像没启动，其实是跨域

这次联调最容易误判的地方，是前端提示“无法连接后端服务”。一开始会以为 Spring Boot 没启动，但命令行直接访问 `http://localhost:8080/user/search` 是 200，说明后端其实在线。

真正的问题是浏览器来源不同：

- 前端页面：`http://127.0.0.1:5173`
- 后端接口：`http://localhost:8080`

虽然它们都指向本机，但浏览器会把 `127.0.0.1` 和 `localhost` 当成不同源。直接请求后端时，如果后端没有配置 CORS，就会被浏览器拦下，Axios 只能拿到 `Network Error`。

## 处理方式

没有动后端代码，而是在 Vite 里加了开发代理：

```ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
}
```

Axios 的默认地址改成 `/api`。这样浏览器看到的是同源请求：

```text
http://127.0.0.1:5173/api/user/search
```

再由 Vite 开发服务器转发到：

```text
http://localhost:8080/user/search
```

这一步不改变后端接口，也没有伪造数据。

## 后端接口现状

当前 Controller 里有：

- `POST /user/register`
- `POST /user/login`
- `POST /user/logout`
- `GET /user/search`
- `POST /user/delete`
- `POST /register-code/generate`
- `GET /register-code/check`

没有看到：

- `GET /user/current`

所以前端在登录成功后可以保存用户信息，但刷新页面后没有官方接口恢复当前用户。退出登录已经能通知后端清理 Session，然后再清理前端状态。

## 注册码接口联调记录

注册码模块目前有三个关键路径：

- 注册接口 `POST /user/register` 需要额外提交 `registerCode`。
- 管理员生成接口 `POST /register-code/generate` 依赖当前 Session 中的管理员身份。
- 校验接口 `GET /register-code/check?code=xxx` 返回布尔值，只表示“是否可用”，不会细分不存在、格式错误或已使用。

前端当前设计：

- 注册页只提供注册码输入框，提交注册时交给后端做最终校验。
- 管理员用户管理页放“生成注册码”和“校验注册码”，方便手动测试这两个接口。
- 前端本地只做 12 位大小写字母或数字的格式校验，业务可用性以后端返回为准。

一个容易误解的点是：校验接口返回 `false` 时，前端无法知道具体原因，只能提示“注册码无效或已被使用”。如果以后想区分“不存在”和“已使用”，后端需要从 `boolean` 改成状态码或响应对象。

## 启动时的一个小坑

如果 IDEA 已经启动了后端，再在命令行运行 `mvn spring-boot:run`，第二个 Spring Boot 会因为 8080 端口被占用而失败：

```text
Web server failed to start. Port 8080 was already in use.
```

这不是依赖或前端的问题，只是同一个端口不能启动两份后端。开发时保持一个后端实例即可。
