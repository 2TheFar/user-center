# user-center

`user-center` 是一个基于 Spring Boot 的用户中心后端项目，当前聚焦于用户注册、登录、登录态维护、管理员查询用户和管理员删除用户等基础能力。项目使用 MyBatis-Plus 访问 MySQL，通过 Session 保存登录态，适合作为用户体系、权限控制和后续业务系统登录模块的起点。

## 项目功能

- 用户注册：校验账号、密码、确认密码，检查账号唯一性，并写入数据库。
- 用户登录：校验账号密码，登录成功后返回脱敏用户信息，并把用户登录态写入 Session。
- 用户退出登录：移除当前 Session 中的用户登录态。
- 用户脱敏：对外返回用户信息时移除密码、逻辑删除标记、更新时间等敏感或内部字段。
- 管理员查询用户：管理员可按用户名模糊查询用户列表。
- 管理员删除用户：管理员可根据用户 ID 删除用户，删除行为由 MyBatis-Plus 逻辑删除配置接管。
- 登录态与权限：使用服务端 Session 保存 `userLoginState`，并通过 `userRole` 区分普通用户和管理员。

## 技术栈

| 技术 | 版本 / 说明 |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Web MVC | HTTP 接口层 |
| MyBatis Spring Boot Starter | 4.0.1 |
| MyBatis-Plus | 3.5.15，提供通用 CRUD、逻辑删除能力 |
| MySQL Connector/J | MySQL 数据库连接 |
| Lombok | 简化实体类 getter、setter 等样板代码 |
| Apache Commons Lang | 字符串校验工具 |
| JUnit 5 / Spring Boot Test | 单元测试与 Spring 上下文测试 |
| Maven Wrapper | 3.3.4，自动使用 Maven 3.9.15 |

## 目录结构

```text
backend
|-- .mvn/wrapper/                         # Maven Wrapper 配置
|-- src
|   |-- main
|   |   |-- java/com/zhiyuan/usercenter
|   |   |   |-- UserCenterApplication.java # Spring Boot 启动类
|   |   |   |-- constant
|   |   |   |   `-- UserConstant.java      # 用户登录态、角色常量
|   |   |   |-- controller
|   |   |   |   `-- UserController.java    # 用户相关 HTTP 接口
|   |   |   |-- mapper
|   |   |   |   `-- UserMapper.java        # MyBatis-Plus Mapper
|   |   |   |-- model/domain
|   |   |   |   |-- User.java              # 用户实体，对应 user 表
|   |   |   |   `-- request
|   |   |   |       |-- UserLoginRequest.java
|   |   |   |       `-- UserRegisterRequest.java
|   |   |   `-- service
|   |   |       |-- UserService.java        # 用户服务接口
|   |   |       `-- impl/UserServiceImpl.java
|   |   `-- resources
|   |       |-- application.yaml            # 应用、数据库、MyBatis-Plus 配置
|   |       `-- mapper/UserMapper.xml       # User 字段映射
|   `-- test
|       `-- java/com/zhiyuan/usercenter
|           |-- UserCenterApplicationTests.java
|           `-- service/UserServiceTest.java
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
|-- HELP.md
`-- README.md
```

## 环境要求

启动项目前请准备：

- JDK 21
- MySQL 8.x 或兼容版本
- 可用的 `user_center` 数据库
- Windows 推荐使用 `mvnw.cmd`，macOS / Linux 使用 `./mvnw`

> 项目已经带有 Maven Wrapper，不强制要求本机提前安装 Maven。

## 数据库准备

当前配置连接本机 MySQL：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: "1234"
    url: jdbc:mysql://localhost:3306/user_center?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
```

如本机账号、密码、端口或数据库名不同，请修改 `src/main/resources/application.yaml`。

可以使用下面的 SQL 初始化数据库和用户表：

```sql
CREATE DATABASE IF NOT EXISTS user_center
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE user_center;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` VARCHAR(256) NULL COMMENT '用户昵称',
  `userAccount` VARCHAR(256) NOT NULL COMMENT '账号',
  `userPassword` VARCHAR(512) NOT NULL COMMENT '密码',
  `avatarUrl` VARCHAR(1024) NULL COMMENT '用户头像',
  `phone` VARCHAR(128) NULL COMMENT '电话',
  `email` VARCHAR(512) NULL COMMENT '邮箱',
  `gender` TINYINT NULL COMMENT '性别',
  `userStatus` INT NOT NULL DEFAULT 0 COMMENT '用户状态',
  `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `userRole` INT NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_userAccount` (`userAccount`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COMMENT='用户';
```

如果需要测试管理员接口，可以先注册一个用户，然后将其设置为管理员：

```sql
UPDATE `user`
SET `userRole` = 1
WHERE `userAccount` = '你的账号';
```

## 配置说明

核心配置位于 `src/main/resources/application.yaml`：

| 配置项 | 当前值 | 说明 |
| --- | --- | --- |
| `spring.application.name` | `user-center` | Spring 应用名 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/user_center...` | MySQL 连接地址 |
| `spring.datasource.username` | `root` | 数据库用户名 |
| `spring.datasource.password` | `1234` | 数据库密码 |
| `server.port` | `8080` | HTTP 服务端口 |
| `server.servlet.session.timeout` | `1d` | Session 有效期 |
| `mybatis-plus.mapper-locations` | `classpath*:/mapper/**/*.xml` | Mapper XML 扫描路径 |
| `mybatis-plus.configuration.map-underscore-to-camel-case` | `false` | 不启用下划线转驼峰，数据库字段需与实体属性名一致 |
| `mybatis-plus.global-config.db-config.logic-delete-field` | `isDeleted` | 逻辑删除字段 |
| `mybatis-plus.global-config.db-config.logic-delete-value` | `1` | 已删除值 |
| `mybatis-plus.global-config.db-config.logic-not-delete-value` | `0` | 未删除值 |

## 启动项目

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
./mvnw spring-boot:run
```

启动成功后，服务默认监听：

```text
http://localhost:8080
```

## 构建与测试

打包：

```powershell
.\mvnw.cmd clean package
```

运行测试：

```powershell
.\mvnw.cmd test
```

注意：当前测试依赖真实 MySQL 数据库和现有数据状态。例如 `UserServiceTest#userRegister` 中会校验账号重复场景，期望数据库中已经存在 `qq1234` 账号；同时测试也会写入 `testAccount1`。如果数据库为空或重复执行测试，可能需要先清理或准备测试数据。

## 接口文档

接口统一前缀：

```text
/user
```

当前接口直接返回业务对象、布尔值、数字或 `null`，还没有统一响应包装。

### 用户注册

```http
POST /user/register
Content-Type: application/json
```

请求体：

```json
{
  "userAccount": "testAccount1",
  "userPassword": "testPassword1",
  "confirmPassword": "testPassword1"
}
```

成功响应：

```json
1
```

返回值说明：

| 返回值 | 含义 |
| --- | --- |
| `> 0` | 注册成功，返回新用户 ID |
| `-1` | 参数为空 |
| `-2` | 账号长度小于 6 位 |
| `-3` | 密码或确认密码长度小于 8 位 |
| `-4` | 账号包含特殊字符，当前仅允许英文字母、数字、下划线 |
| `-5` | 两次输入的密码不一致 |
| `-6` | 账号已存在，或保存用户失败 |
| `null` | Controller 层收到空请求体或必要字段为空 |

示例：

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testAccount1","userPassword":"testPassword1","confirmPassword":"testPassword1"}'
```

### 用户登录

```http
POST /user/login
Content-Type: application/json
```

请求体：

```json
{
  "userAccount": "testAccount1",
  "userPassword": "testPassword1"
}
```

成功响应示例：

```json
{
  "id": 1,
  "username": null,
  "userAccount": "testAccount1",
  "userPassword": null,
  "avatarUrl": null,
  "phone": null,
  "email": null,
  "gender": null,
  "userStatus": 0,
  "createTime": "2026-05-21T00:00:00.000+00:00",
  "updateTime": null,
  "isDeleted": null,
  "userRole": 0
}
```

登录成功后，后端会把脱敏后的用户对象写入当前 Session：

```text
Session key: userLoginState
```

失败时返回 `null`。常见失败原因包括账号或密码为空、账号长度不合法、密码长度不合法、账号包含特殊字符、账号密码不匹配。

示例：

```bash
curl -i -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testAccount1","userPassword":"testPassword1"}'
```

如果后续要访问管理员接口，需要保留登录响应中的 `JSESSIONID` Cookie。

### 用户退出登录

```http
POST /user/logout
```

行为：

- 移除当前 Session 中的 `userLoginState`。
- 成功返回 `true`。
- 如果请求对象异常，返回 `false`。

示例：

```bash
curl -X POST http://localhost:8080/user/logout \
  -H "Cookie: JSESSIONID=你的会话ID"
```

### 管理员查询用户

```http
GET /user/search?username=otaku
```

权限要求：

- 必须已登录。
- 当前 Session 中的用户 `userRole` 必须为 `1`。

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `username` | 否 | 用户昵称，传入时进行模糊查询；不传则查询全部未删除用户 |

成功响应：

```json
[
  {
    "id": 1,
    "username": "otaku",
    "userAccount": "testAccount1",
    "userPassword": null,
    "avatarUrl": "https://example.com/avatar.png",
    "phone": "13812345678",
    "email": "123@qq.com",
    "gender": 0,
    "userStatus": 0,
    "createTime": "2026-05-21T00:00:00.000+00:00",
    "updateTime": null,
    "isDeleted": null,
    "userRole": 1
  }
]
```

非管理员或未登录时返回 `null`。

示例：

```bash
curl "http://localhost:8080/user/search?username=otaku" \
  -H "Cookie: JSESSIONID=你的会话ID"
```

### 管理员删除用户

```http
POST /user/delete
Content-Type: application/json
```

权限要求：

- 必须已登录。
- 当前 Session 中的用户 `userRole` 必须为 `1`。

请求体是用户 ID 数字本身：

```json
1
```

响应：

```json
true
```

返回值说明：

| 返回值 | 含义 |
| --- | --- |
| `true` | 删除成功 |
| `false` | 未登录、非管理员、ID 非法或删除失败 |

示例：

```bash
curl -X POST http://localhost:8080/user/delete \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=你的会话ID" \
  -d '1'
```

## 核心业务规则

### 注册校验

注册逻辑位于 `UserServiceImpl#userRegister`：

- `userAccount`、`userPassword`、`confirmPassword` 不能为空。
- 账号长度不能小于 6。
- 密码和确认密码长度不能小于 8。
- 账号只能包含英文字母、数字和下划线，正则为 `^[a-zA-Z0-9_]+$`。
- 密码和确认密码必须完全一致。
- 账号不能重复。
- 密码存储前会使用固定盐值 `kawaii` 拼接原始密码，再进行 MD5 摘要。

### 登录校验

登录逻辑位于 `UserServiceImpl#userLogin`：

- 账号和密码不能为空。
- 账号长度不能小于 6。
- 密码长度不能小于 8。
- 账号只能包含英文字母、数字和下划线。
- 输入密码经过同样的盐值和 MD5 处理后，与数据库中的 `userPassword` 匹配。
- 登录成功后写入 Session。

### 用户脱敏

脱敏逻辑位于 `UserServiceImpl#getSafeUser`。当前对外保留字段：

- `id`
- `username`
- `userAccount`
- `avatarUrl`
- `phone`
- `email`
- `gender`
- `userRole`
- `userStatus`
- `createTime`

不会主动返回：

- `userPassword`
- `updateTime`
- `isDeleted`

## 角色与权限

角色常量定义在 `UserConstant`：

| 常量 | 值 | 说明 |
| --- | --- | --- |
| `DEFAULT_ROLE` | `0` | 普通用户 |
| `ADMIN_ROLE` | `1` | 管理员 |
| `USER_LOGIN_STATE` | `userLoginState` | Session 中保存登录用户的 key |

管理员判断逻辑位于 `UserController#isAdmin`：从 Session 中读取 `userLoginState`，再判断 `userRole == 1`。

## 数据模型

`User` 实体对应数据库中的 `user` 表：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键，自增 |
| `username` | `VARCHAR` | 用户昵称 |
| `userAccount` | `VARCHAR` | 账号 |
| `userPassword` | `VARCHAR` | 加密后的密码 |
| `avatarUrl` | `VARCHAR` | 用户头像 |
| `phone` | `VARCHAR` | 电话 |
| `email` | `VARCHAR` | 邮箱 |
| `gender` | `TINYINT` | 性别 |
| `userStatus` | `INT` | 用户状态 |
| `createTime` | `DATETIME` | 创建时间 |
| `updateTime` | `DATETIME` | 更新时间 |
| `isDeleted` | `TINYINT` | 逻辑删除标记 |
| `userRole` | `INT` | 用户角色，0-普通用户，1-管理员 |

由于 `map-underscore-to-camel-case` 当前设置为 `false`，数据库字段名需要保持 `userAccount`、`userPassword`、`avatarUrl` 这类驼峰命名，否则需要同步调整配置或 Mapper 映射。

## 开发提示

- `UserMapper` 继承 `BaseMapper<User>`，基础 CRUD 由 MyBatis-Plus 提供。
- `UserService` 继承 `IService<User>`，可以直接使用 `save`、`list`、`count`、`removeById` 等通用方法。
- `UserController` 当前只做了简单参数判空和权限判断，复杂错误码主要由 Service 返回。
- `removeById` 在当前 MyBatis-Plus 逻辑删除配置下会更新 `isDeleted`，而不是物理删除记录。
- 当前项目没有统一异常处理、统一响应对象和全局错误码，接口调用方需要直接处理 `null`、负数、布尔值等不同返回形式。

## 常见问题

### 启动时报数据库连接失败

检查：

- MySQL 是否已启动。
- `user_center` 数据库是否已创建。
- `application.yaml` 中账号、密码、端口是否正确。
- MySQL 是否允许当前用户从本机连接。

### 注册后登录失败

检查：

- 登录时使用的明文密码是否与注册时一致。
- 数据库中 `userPassword` 是否是通过当前代码生成的 MD5 值。
- 账号是否只包含英文字母、数字和下划线。
- 账号长度是否不少于 6，密码长度是否不少于 8。

### 管理员接口返回 null 或 false

检查：

- 是否先调用 `/user/login` 完成登录。
- 后续请求是否携带同一个 `JSESSIONID` Cookie。
- 当前登录用户的 `userRole` 是否为 `1`。

### 测试失败

当前测试不是纯内存测试，会依赖本地 MySQL 和测试数据。建议在运行测试前准备独立测试库，或者在后续改造中引入测试容器、H2、测试 Profile 和数据初始化脚本。

## 后续可改进方向

- 增加统一响应包装，例如 `BaseResponse<T>`。
- 增加全局异常处理和业务错误码枚举。
- 将数据库密码、密码盐值等敏感配置移动到环境变量或独立配置文件。
- 使用 BCrypt、Argon2 等更适合密码存储的哈希算法替代 MD5。
- 增加获取当前登录用户接口。
- 完善权限控制，避免在 Controller 中手写管理员判断。
- 增加参数校验注解，例如 `@Valid`、`@NotBlank`、`@Size`。
- 增加测试隔离能力，避免测试依赖固定账号和本地数据库状态。
- 增加接口文档生成能力，例如 OpenAPI / Swagger。
