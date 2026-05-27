# 用户中心

这是一个前后端分离的用户中心项目，根目录只保留仓库级说明和公共文档，后端与前端分别放在独立目录中。

## 目录结构

```text
user-center/
  backend/       Spring Boot 后端
  frontend/      Vue 3 + Vite 前端
  project-docs/  开发记录、接口联调笔记和报告
```

## 后端启动

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认监听：

```text
http://localhost:8080
```

## 前端启动

```powershell
cd frontend
npm install
npm run dev
```

前端默认监听：

```text
http://127.0.0.1:5173
```

开发环境下，前端通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`，避免浏览器跨域请求后端。

## 常用检查

前端：

```powershell
cd frontend
npm run lint
npm run build
```

后端：

```powershell
cd backend
.\mvnw.cmd test
```

## 文档

- [后端说明](./backend/README.md)
- [开发文档目录](./project-docs/README.md)
- [前端开发报告](./project-docs/frontend-dev-report.md)
- [接口联调笔记](./project-docs/api-integration-notes.md)
