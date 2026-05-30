# TODO

## 登录态刷新恢复

- [x] 后端补充 `GET /user/current` 接口。

  用于在页面刷新后，根据浏览器自动携带的 `JSESSIONID` Cookie 从后端 Session 中读取 `USER_LOGIN_STATE`，返回当前登录用户。

- [x] 前端应用启动时调用 `/user/current`。

  如果后端返回当前用户，则重新写入 Pinia 的 `currentUser`；如果后端返回未登录，则清空前端当前用户状态。

### 背景说明

当前项目登录成功后，后端已经把用户信息保存到 Session，浏览器也会保存对应的 Session Cookie。刷新页面时，后端 Session 通常仍然存在，但前端 Pinia 是内存状态，会随页面刷新重新初始化为 `null`。

因此刷新后看起来像“登录丢了”，本质上通常是前端忘了当前用户是谁，而不是后端一定丢失了登录态。当前已经通过 `GET /user/current` 实现恢复：前端刷新后重新向后端询问“当前用户是谁”，再恢复 Pinia 状态。
