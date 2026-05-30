# 用户中心文档小站

这里放和本次前端开发有关的记录。它不是特别正式的企业级文档，更像一个项目旁边的开发手账：为什么这么做、踩了什么坑、后面接着改时要先看哪里。

## 文档目录

- [前端开发报告](./frontend-dev-report.md)：本次 Vue 3 前端从搭建到联调的完整记录。
- [前端更新日志 2026-05-30](./前端更新_20260530.md)：参考教程思路并按当前项目标准实现后的前端优化记录。
- [CodeFather Vue 前端教程整理笔记](./codefather-vue-frontend-tutorial-notes.md)：从用户复制的教程文本中筛选出的 Vue 前端路线和当前项目适配说明。
- [原始前端生成提示词](./original-frontend-generation-prompt.md)：早期生成当前前端项目时使用的提示词归档。
- [接口联调笔记](./api-integration-notes.md)：后端接口、Session、Vite 代理和跨域问题的处理记录。
- [后端统一返回与异常处理记录](./backend-response-exception-notes.md)：`BaseResponse<T>`、`ErrorCode`、`BusinessException` 和全局异常处理器的说明。
- [开发随手记](./dev-diary.md)：一些更口语化的开发过程记录，方便回忆上下文。

## 当前状态

项目现在是单仓库下的前后端分离结构：根目录放 Spring Boot 后端，`frontend` 放 Vue 3 前端。开发时后端仍然由 IDEA 或 Maven 启动，前端由 Vite 启动。

常用命令：

```powershell
cd frontend
npm install
npm run dev
npm run lint
npm run build
```

后端接口通过 `/api` 代理到 `http://localhost:8080`，这样浏览器不会直接跨域访问后端。退出登录已经接入后端的 `POST /user/logout` 接口。

后端现在已经统一响应格式：Controller 返回 `BaseResponse<T>`，业务失败抛出 `BusinessException`，再由 `GlobalExceptionHandler` 转换为统一的错误响应。前端请求层已经集中处理该结构，业务代码通常直接拿到响应里的 `data`。

邀请码模块当前已经接入前端：

- 注册页新增邀请码输入框，提交注册时仍按后端字段 `registerCode` 传给 `POST /user/register`。
- 管理员用户管理页新增邀请码工具区，可以生成邀请码，也可以手动校验邀请码是否可用。
- 注册页不单独放“校验邀请码”按钮，最终可用性以后端注册接口为准。
- 2026-05-30 更新后，页面文案已经按正式应用服务调整，不再向普通用户暴露后端、Session、接口等实现细节。
