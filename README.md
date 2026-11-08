# 苍穹外卖（sky-take-out）

> 基于 Spring Boot 的外卖点餐系统后端服务，分为**管理端**与**用户端**两套接口，覆盖菜品管理、订单流转、购物车、微信支付、来单提醒与运营数据报表等完整业务。

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.3-6DB33F?logo=springboot&logoColor=white)
![JDK](https://img.shields.io/badge/JDK-17-007396?logo=openjdk&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-2.2.0-C71A36)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)
![用途](https://img.shields.io/badge/%E7%94%A8%E9%80%94-%E5%AD%A6%E4%B9%A0%E4%BA%A4%E6%B5%81-blue)

本项目为学习实践项目，业务与课程源自**黑马程序员《苍穹外卖》**，仅用于学习交流。

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [接口文档](#接口文档)
- [致谢与声明](#致谢与声明)

---

## 功能特性

### 管理端（`/admin/**`）

- **员工管理**：登录鉴权、员工增删改查、启用/禁用、密码修改
- **分类管理**：菜品分类、套餐分类的维护
- **菜品管理**：菜品与口味的增删改查、批量删除、起售/停售、图片上传
- **套餐管理**：套餐与菜品关联维护、起售/停售
- **订单管理**：订单查询、接单/拒单、取消、派送、完成等全流程流转
- **工作台**：今日订单、菜品/套餐概览、订单状态统计
- **数据统计**：营业额、用户、订单、销量 Top10 报表，支持导出 Excel 运营报表
- **店铺管理**：营业状态设置与查询
- **来单提醒**：基于 WebSocket 的来单/催单语音提醒
- **定时任务**：每分钟处理超时未支付订单，每日凌晨处理派送中订单

### 用户端（`/user/**`）

- **微信登录**：小程序 code 换取 openid 登录
- **商品浏览**：分类、菜品、套餐查询
- **购物车**：添加、删减、清空、查看
- **地址簿**：收货地址增删改查、默认地址
- **订单**：下单、微信支付、催单、取消、历史订单、再来一单
- **店铺状态**：查询当前是否营业

---

## 技术栈

| 分类 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| 基础框架 | Spring Boot | 2.7.3 | 应用主框架 |
| 持久层 | MyBatis | 2.2.0 | ORM 与 XML Mapper |
| 数据库 | MySQL | 5.7 / 8.0 | 业务数据存储 |
| 连接池 | Druid | 1.2.1 | 数据库连接池 |
| 分页 | PageHelper | 1.3.0 | 物理分页 |
| 缓存 | Spring Cache + Redis | - | 缓存与营业状态、验证码等 |
| 鉴权 | JWT (jjwt) | 0.9.1 | 管理端/用户端令牌 |
| 接口文档 | Knife4j (Swagger) | 3.0.2 | 在线接口调试 |
| 对象存储 | 阿里云 OSS | 3.10.2 | 菜品图片上传 |
| 支付 | 微信支付 APIv3 | 0.4.8 | 下单支付与退款回调 |
| 实时通信 | Spring WebSocket | - | 来单/催单提醒 |
| 报表 | Apache POI | 3.16 | Excel 运营报表导出 |
| AOP | AspectJ | 1.9.4 | 公共字段自动填充 |
| 工具 | Lombok / FastJSON / Commons-Lang | - | 简化样板代码与 JSON 处理 |
| 前端 | Vue + TypeScript | - | 管理端源码（独立资料，不在本仓库） |

---

## 项目结构

本项目为 Maven 多模块工程，根 `artifactId` 为 `sky-take-out`：

```
sky-take-out
├── sky-common          # 通用模块
│   └── com.sky
│       ├── constant        # 常量
│       ├── context         # 线程上下文（BaseContext）
│       ├── enumeration     # 枚举
│       ├── exception       # 自定义业务异常
│       ├── json            # Jackson 序列化配置
│       ├── properties      # 配置属性绑定（JWT / OSS / WeChat）
│       ├── result          # 统一返回结果 Result / PageResult
│       └── utils           # 工具类（JWT / OSS / HttpClient / WeChatPay）
├── sky-pojo            # 数据模型模块
│   └── com.sky
│       ├── dto             # 请求数据传输对象
│       ├── entity          # 数据库实体
│       └── vo              # 视图返回对象
└── sky-server          # 主服务模块（可执行）
    ├── com.sky
    │   ├── SkyApplication      # 启动类
    │   ├── annotation          # 自定义注解（AutoFill）
    │   ├── aspect              # AOP 切面（公共字段自动填充）
    │   ├── config              # 配置类（Web / Redis / OSS / WebSocket）
    │   ├── controller          # 控制器（admin / user / notify）
    │   ├── handler             # 全局异常处理
    │   ├── interceptor         # JWT 拦截器
    │   ├── mapper              # MyBatis Mapper 接口
    │   ├── service             # 业务接口与实现
    │   ├── task                # 定时任务
    │   └── websocket           # WebSocket 服务
    └── resources
        ├── application.yml     # 主配置（含占位符，需搭配 application-dev.yml）
        ├── mapper/*.xml        # MyBatis XML 映射
        └── template/*.xlsx     # 运营数据报表模板
```

---

## 环境要求

| 依赖 | 版本要求 |
| --- | --- |
| JDK | 17 |
| Maven | 3.6+ |
| MySQL | 5.7 或 8.0 |
| Redis | 5.0+ |

> 项目 `pom.xml` 已显式指定 Java 17，并为 JDK 17 下的 Lombok 配置了编译参数，请务必使用 **JDK 17** 编译运行。

---

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/deservehy-collab/cqwm.git
cd cqwm
```

### 2. 初始化数据库

使用课程提供的 `sky.sql` 初始化数据库（该脚本会自动创建 `sky_take_out` 库及 11 张表）：

```bash
mysql -u root -p < sky.sql
```

> `sky.sql` 属于课程配套资料，**不包含在本仓库中**，请自行准备。

### 3. 新建开发环境配置

仓库仅提供 `application.yml`（含占位符），**不包含 `application-dev.yml`**。请在 `sky-server/src/main/resources/` 下新建 `application-dev.yml`，并填入你自己的配置（以下为占位符模板，切勿提交真实密钥）：

```yaml
sky:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: localhost
    port: 3306
    database: sky_take_out
    username: your-mysql-username
    password: your-mysql-password
  redis:
    host: localhost
    port: 6379
    database: 1
  alioss:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: your-bucket-name
  wechat:
    appid: your-wechat-appid
    secret: your-wechat-secret
    mchid: your-mchid
    mchSerialNo: your-mch-serial-no
    apiV3Key: your-api-v3-key
    notifyUrl: https://your-domain/notify/paySuccess
    refundNotifyUrl: https://your-domain/notify/refundSuccess
```

> 微信登录/支付、阿里云 OSS 图片上传需填入**你自己的真实密钥**才能使用；仅体验管理端基础功能时，可先专注数据库与 Redis 配置。

### 4. 启动 Redis

确保本地 Redis 已启动（默认 `localhost:6379`）。

### 5. 编译并运行

```bash
# 方式一：直接运行
mvn -pl sky-server -am spring-boot:run

# 方式二：打包后运行
mvn clean package
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
```

服务默认监听 **`http://localhost:8080`**。

### 6. 访问接口文档与登录

- 接口文档（Knife4j）：`http://localhost:8080/doc.html`
- 管理端登录接口：`POST /admin/employee/login`
- 默认管理员账号：**`admin` / `123456`**

> 登录时后端会对输入密码做 MD5 后再与库中密文比对，因此 `employee.password` 字段需存储密码的 MD5 值（`123456` 对应 `e10adc3949ba59abbe56e057f20f883e`）。若使用初始账号无法登录，请检查该字段是否为 MD5 密文。

### 7. 前端（可选）

管理端前端为 `Vue + TypeScript` 工程（`vue-typescript-admin-template`），属于独立配套资料，不在本仓库中。前端通过 Nginx 反向代理将 `/api/**` 转发到后端 `8080` 端口后联调。

---

## 配置说明

主配置 `application.yml` 通过占位符引用 `application-dev.yml` 中的敏感配置，关键项如下：

| 配置项 | 说明 |
| --- | --- |
| `sky.datasource.*` | MySQL 连接信息（host / port / database / username / password） |
| `sky.redis.*` | Redis 连接信息（host / port / database） |
| `sky.jwt.*` | JWT 签名密钥、过期时间、令牌名称（已在 `application.yml` 中给出默认值） |
| `sky.alioss.*` | 阿里云 OSS 的 endpoint / accessKeyId / accessKeySecret / bucketName |
| `sky.wechat.*` | 微信小程序与支付的 appid / secret / mchid / 证书序列号 / apiV3Key / 回调地址 |

> **安全提示**：`application-dev.yml` 含数据库密码与第三方密钥，请勿提交到版本库；建议在 `.gitignore` 中忽略该文件。

---

## 接口文档

项目集成 Knife4j（Swagger 增强 UI），启动后访问：

```
http://localhost:8080/doc.html
```

可在线查看并调试管理端（`/admin/**`）与用户端（`/user/**`）全部接口。除登录、店铺状态等公开接口外，其余接口需在请求头携带 JWT 令牌（管理端令牌名 `token`，用户端令牌名 `authentication`）。

---

## 致谢与声明

- 本项目业务设计与教学资料来自 **黑马程序员（itcast）《苍穹外卖》** 课程，特此致谢。
- 仓库仅用于个人学习与技术交流，原始课程资料与相关版权归原作者所有。
- 如有侵权请联系删除。