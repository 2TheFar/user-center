# 用户中心 Vue 前端教程整理笔记

> 来源：用户手动复制的 CodeFather 页面文本。
> 原页面标题：用户中心项目前端教程（Vue 入门版）。
> 整理时间：2026-05-30。
> 说明：本文不是原文全文转存，而是从复制文本中筛选 Vue 前端开发相关内容，去除了页面导航、评论、图片占位、复制按钮文字和随机水印串，并结合当前项目结构做了适配备注。

## 教程定位

这节教程假设用户中心后端已经完成，并且已经提供可调用的 API。前端主要负责登录、注册、用户管理、请求封装、全局登录态维护和部署配置。

适合两类学习路径：

- 后端方向或前端基础较弱：先把后端学完并理解接口，再单独学习本节 Vue 前端。
- 已有前端框架基础：可以直接按 Vue 版本实现，也可以参考原 React 版本，对比不同技术栈的实现方式。

## 教程大纲

1. 需求分析。
2. Web 前端技术选型。
3. Web 前端项目初始化。
4. 通用布局。
5. 路由。
6. 请求封装。
7. 前端页面开发。
8. 用户登录与 Cookie / Session。
9. 用户注册。
10. 用户管理。
11. 权限控制。
12. 项目部署与多环境配置。

## 技术选型

教程中给出的 Vue 入门版技术栈如下：

| 技术 | 作用 |
| --- | --- |
| Vue 3 | 主流前端框架 |
| Vue-CLI | 快速创建 Vue 项目 |
| Ant Design Vue | 快速开发 UI 页面 |
| Axios | 向后端发送 HTTP 请求 |
| Pinia | 维护前端全局状态 |
| ESLint + Prettier + TypeScript | 代码规范、格式化和类型校验 |

当前项目已经采用：

| 当前项目 | 说明 |
| --- | --- |
| Vue 3 + Vite | 比 Vue-CLI 更现代，启动和构建更快 |
| Ant Design Vue 4.x | 与教程组件库方向一致 |
| Axios | 与教程一致 |
| Pinia | 与教程一致 |
| TypeScript + ESLint + Prettier | 与教程工程化方向一致 |

因此后续实现可以沿用教程思路，但不用退回 Vue-CLI。

## 项目初始化

教程中的初始化方式：

```bash
node -v
npm install -g @vue/cli
vue -V
vue create user-center-frontend-vue
npm run serve
```

当前项目已经存在 `frontend/`，并使用 Vite：

```bash
cd frontend
npm install
npm run dev
npm run build
```

适配建议：

- 保留当前 Vite 项目，不再新建 Vue-CLI 项目。
- 路由、请求、状态管理、页面组件的实现思路仍然可以按教程推进。
- 多环境配置优先使用 Vite 的 `import.meta.env` 和 `.env.*` 文件。

## 工程化配置

教程建议使用脚手架内置的 Prettier、ESLint、TypeScript 和格式化插件，保证代码风格统一。

当前项目已经具备：

- `eslint.config.js`
- `tsconfig*.json`
- `package.json` 中的 `lint`、`format`、`build` 脚本

建议开发流程：

```bash
cd frontend
npm run lint
npm run build
```

如果本地 IDE 格式化效果不稳定，以 `npm run format` 和 ESLint 结果为准。

## 引入 Ant Design Vue

教程安装方式：

```bash
npm i --save ant-design-vue@4.x
```

入口文件示意：

```ts
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

createApp(App).use(Antd).use(router).mount('#app');
```

当前项目已经引入 Ant Design Vue，并且在 `App.vue` 中使用了 `a-config-provider` 配置中文语言包和主题 token。

## 开发规范

教程建议使用 Vue 3 组合式 API。

推荐写法：

```vue
<script setup lang="ts">
import { ref } from 'vue';

const message = ref('hello');
</script>
```

当前项目也已经使用 `<script setup lang="ts">`，可以继续保持。

## 全局布局

教程建议：

- 新建 `layouts/BasicLayout.vue`。
- 使用 Ant Design Vue 的 `Layout` 组件组织上、中、下结构。
- 使用 `router-view` 承载动态页面内容。
- 新建 `components/GlobalHeader.vue` 维护顶部导航。

当前项目已经有相近结构：

- `src/components/AppHeader.vue`
- `src/components/AuthShell.vue`
- `src/views/LoginView.vue`
- `src/views/RegisterView.vue`
- `src/views/UserManagementView.vue`

适配建议：

- 后台页继续使用 `AppHeader` + `a-layout`。
- 登录和注册页继续使用 `AuthShell`。
- 若后续页面增多，可再抽出统一的 `BasicLayout.vue`，避免每个后台页面重复布局。

## 路由

教程目标：

- 点击菜单项后跳转到对应页面。
- 刷新页面后，菜单仍能根据当前 URL 自动高亮。

教程路由示例：

```ts
const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/user/login', name: 'userLogin', component: HomeView },
  { path: '/user/register', name: 'userRegister', component: HomeView },
  { path: '/admin/userManage', name: 'adminUserManage', component: HomeView },
];
```

当前项目路由：

```ts
const routes = [
  { path: '/', redirect: '/users' },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
  { path: '/users', name: 'users', component: () => import('@/views/UserManagementView.vue') },
];
```

适配建议：

- 当前路由更短，适合小型项目。
- 如果想更贴近教程，可改为 `/user/login`、`/user/register`、`/admin/userManage`。
- 管理页应补路由守卫，未登录或非管理员时跳转登录页。

## 请求封装

教程建议使用 Axios，并在全局请求实例中配置：

- `baseURL`
- `timeout`
- `withCredentials: true`
- 请求拦截器
- 响应拦截器

`withCredentials: true` 很关键。后端使用 Session 登录态时，浏览器必须在后续请求中携带 Cookie，否则无法维持登录。

教程请求实例核心思路：

```ts
import axios from 'axios';

const myAxios = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  withCredentials: true,
});

myAxios.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.code === 40100) {
      // 未登录时跳转登录页
    }
    return response;
  },
  (error) => Promise.reject(error),
);

export default myAxios;
```

当前项目已经有 `frontend/src/api/request.ts`，并且使用了：

```ts
baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
withCredentials: true,
```

适配建议：

- 当前项目通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`，这是比前端直接请求后端端口更舒服的本地开发方式。
- 响应拦截器应适配后端统一响应 `BaseResponse<T>`，判断 `code === 0` 后再返回 `data`。
- 遇到 `40100` 可以清理前端用户状态并跳转登录。
- 遇到 `40101` 可以提示无权限。

## 用户相关 API

教程建议把用户接口集中放到 `src/api/user.ts`。

典型接口：

```ts
export const userRegister = async (params: any) => {
  return myAxios.request({
    url: '/api/user/register',
    method: 'POST',
    data: params,
  });
};

export const userLogin = async (params: any) => {
  return myAxios.request({
    url: '/api/user/login',
    method: 'POST',
    data: params,
  });
};

export const searchUsers = async (username: any) => {
  return myAxios.request({
    url: '/api/user/search',
    method: 'GET',
    params: { username },
  });
};

export const deleteUser = async (id: string) => {
  return myAxios.request({
    url: '/api/user/delete',
    method: 'POST',
    data: id,
    headers: {
      'Content-Type': 'application/json',
    },
  });
};
```

当前项目已经实现：

- `registerUser`
- `loginUser`
- `getCurrentUser`
- `logoutUser`
- `searchUsers`
- `deleteUser`
- `generateRegisterCode`
- `checkRegisterCode`

其中注册码相关接口是当前后端的增强能力，教程原始版本里对应字段可能是 `planetCode`，当前项目应使用 `registerCode`。

## Pinia 全局状态

教程建议用 Pinia 保存全局登录用户：

```ts
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<any>({
    username: '未登录',
  });

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser;
  }

  return { loginUser, setLoginUser };
});
```

当前项目已经有 `frontend/src/stores/user.ts`：

- `currentUser`
- `isLoggedIn`
- `isAdmin`
- `setCurrentUser`
- `clearCurrentUser`
- `fetchCurrentUser`

适配建议：

- 当前项目已经补充 `/user/current` 接口，前端可以在应用启动时自动恢复登录态。
- 刷新页面后 Pinia 内存状态会重新初始化，但前端会通过 `/user/current` 根据后端 Session 重新写入当前用户。

## 用户登录页面

教程登录页核心：

- 使用 Ant Design Vue 表单组件。
- 账号必填。
- 密码必填且不少于 8 位。
- 调用登录接口。
- 登录成功后更新 Pinia 状态并跳转首页。

关键逻辑：

```ts
const handleSubmit = async () => {
  const res = await userLogin(form);
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser();
    message.success('登录成功');
    router.push({ path: '/', replace: true });
  } else {
    message.error('登录失败');
  }
};
```

当前项目适配：

- 登录页是 `frontend/src/views/LoginView.vue`。
- 登录成功后保存 `currentUser`，跳转 `/users`。
- 建议增加 redirect 参数支持：用户访问管理页被拦截后，登录成功应回到原页面。

## Cookie / Session 登录态

教程讲解的流程：

1. 前端第一次连接服务器，服务端创建 Session。
2. 登录成功后，后端把用户信息写入 Session。
3. 后端通过响应要求浏览器保存 Cookie。
4. 浏览器后续请求同域接口时自动携带 Cookie。
5. 后端根据 Cookie 找到对应 Session，再读取登录用户信息。

当前项目注意点：

- Axios 必须开启 `withCredentials: true`。
- 本地开发时推荐使用 Vite 代理，减少跨域 Cookie 问题。
- 应用启动时调用 `/user/current`，用后端 Session 恢复 Pinia 中的当前用户。
- 后端接口权限仍以后端 Session 为准，前端的 Pinia 只用于展示和交互优化，不能作为真正安全边界。

## 用户注册页面

教程注册页核心：

- 账号。
- 密码。
- 确认密码。
- 编号或注册码。
- 提交前校验两次密码是否一致。
- 注册成功后跳转登录页。

教程原始字段可能是：

```ts
const form = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  planetCode: '',
});
```

当前项目后端字段是：

```ts
const form = reactive({
  userAccount: '',
  userPassword: '',
  confirmPassword: '',
  registerCode: '',
});
```

适配建议：

- 账号：不少于 6 位，只允许字母、数字、下划线。
- 密码：不少于 8 位。
- 注册码：12 位大小写字母或数字。
- 提交失败时使用后端 `message` 展示真实原因。

## 用户管理页面

教程需求：

- 管理员查看已注册用户。
- 支持按用户名搜索。
- 支持删除非法用户。
- 普通用户不能查看用户信息。

页面结构：

- 上方搜索栏。
- 下方用户表格。
- 操作列放删除按钮。

教程表格列：

```ts
const columns = [
  { title: 'id', dataIndex: 'id' },
  { title: '用户名', dataIndex: 'username' },
  { title: '账号', dataIndex: 'userAccount' },
  { title: '头像', dataIndex: 'avatarUrl' },
  { title: '性别', dataIndex: 'gender' },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '用户角色', dataIndex: 'userRole' },
  { title: '操作', key: 'action' },
];
```

教程建议使用插槽优化展示：

- `avatarUrl` 用图片或头像组件展示。
- `userRole` 用 Tag 展示管理员 / 普通用户。
- `createTime` 格式化为可读时间。
- `action` 放删除按钮。

当前项目已经实现：

- 用户搜索。
- 用户表格。
- 头像展示。
- 性别、角色、状态格式化。
- 创建时间格式化。
- 删除确认。
- 管理员注册码生成和校验工具。

建议继续增强：

- 删除成功后刷新表格。
- 删除当前登录用户时给出二次确认或禁用。
- 非管理员访问时展示更明确的空状态和登录引导。
- 如果用户量增大，应后端分页，前端表格接分页参数。

## 权限控制

教程给出两种方式：

- 在单个页面内判断权限。
- 使用全局路由守卫统一判断权限。

更推荐全局路由守卫：

```ts
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore();
  const loginUser = loginUserStore.loginUser;
  if (to.fullPath.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 1) {
      message.error('没有权限');
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
  }
  next();
});
```

当前项目适配建议：

- 如果保持 `/users` 作为管理页，可以在路由 meta 中标记：

```ts
{
  path: '/users',
  name: 'users',
  component: () => import('@/views/UserManagementView.vue'),
  meta: { requiresLogin: true, requiresAdmin: true },
}
```

- 在 `router.beforeEach` 中读取 meta 判断权限。
- 当前后端已经有 `/user/current`，刷新后前端可以恢复用户状态；权限守卫仍应和后端接口错误处理配合使用。

## 多环境与部署

教程中的多环境分类：

- 本地环境：自己的电脑。
- 开发环境：远程开发，供多人联调。
- 测试环境：测试人员验证功能。
- 预发布环境：尽量与正式环境一致。
- 正式环境：公开对外，尽量保持稳定。
- 沙箱环境：实验用途。

教程中 Vue-CLI 请求地址示例：

```ts
const myAxios = axios.create({
  baseURL:
    process.env.NODE_ENV === 'development'
      ? 'http://localhost:8080'
      : '线上地址',
  timeout: 10000,
  withCredentials: true,
});
```

当前 Vite 项目建议：

```env
# .env.development
VITE_API_BASE_URL=/api
```

```env
# .env.production
VITE_API_BASE_URL=https://your-api.example.com
```

当前 `vite.config.ts` 本地代理：

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

构建命令：

```bash
cd frontend
npm run build
```

构建产物在 `frontend/dist/`，可以部署到静态网站服务或由后端托管。

## 对当前项目的落地清单

- 保留 Vite，不切换 Vue-CLI。
- 保留 Ant Design Vue，继续用现有组件。
- 请求层统一解包 `BaseResponse<T>`。
- 明确处理 `40100` 未登录和 `40101` 无权限。
- 登录成功保存 Pinia 用户状态。
- 注册页字段使用当前后端的 `confirmPassword` 和 `registerCode`。
- 管理页继续对接 `/user/search` 和 `/user/delete`。
- 邀请码工具继续对接 `/register-code/generate` 和 `/register-code/check`。
- 已补 `/user/current`，应用启动时会尝试恢复刷新后的前端登录态。
- 管理页权限最好用路由 meta + 后端权限错误双保险。
