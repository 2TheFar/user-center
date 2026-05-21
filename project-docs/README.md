# 用户中心文档小站

这里放和本次前端开发有关的记录。它不是特别正式的企业级文档，更像一个项目旁边的开发手账：为什么这么做、踩了什么坑、后面接着改时要先看哪里。

## 文档目录

- [前端开发报告](./frontend-dev-report.md)：本次 Vue 3 前端从搭建到联调的完整记录。
- [接口联调笔记](./api-integration-notes.md)：后端接口、Session、Vite 代理和跨域问题的处理记录。
- [开发随手记](./dev-diary.md)：一些更口语化的开发过程记录，方便回忆上下文。

## 当前状态

前端项目已经放在仓库主目录，和 Spring Boot 后端共用一个仓库。开发时后端仍然由 IDEA 或 Maven 启动，前端由 Vite 启动。

常用命令：

```powershell
npm install
npm run dev
npm run lint
npm run build
```

后端接口通过 `/api` 代理到 `http://localhost:8080`，这样浏览器不会直接跨域访问后端。
