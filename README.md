# 点菜系统 · order-web

一个 **Docker 化的餐厅点菜系统**：顾客输入桌号即可浏览菜单、加购物车、提交订单；后台管理菜品（增删改查、图片上传、上下架）并处理订单。

前端 **Vue 3 + Vite**，后端 **Spring Boot 3 + MyBatis-Plus**，数据库 **MySQL 8**，**Nginx** 统一入口，**Docker Compose** 一键启动。

---

## 功能

### 顾客端
- 输入桌号直接进入点菜界面（无需注册登录）
- 按分类浏览菜品，显示图片 / 描述 / 价格
- 购物车数量加减、实时合计
- 填写备注（少辣、不要香菜等）
- 提交订单，显示订单号与金额

### 后台管理
- 简单账号密码登录（默认 `admin` / `admin123`）
- 菜品增删改查
- 图片上传（保存到宿主机 `./uploads`，Nginx 直出）
- 一键上架 / 下架
- 分类管理（新增 / 删除）
- 订单列表，按状态筛选，标记完成 / 恢复

---

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3、Vue Router 4、Vite 5、原生 CSS |
| 后端 | Spring Boot 3.2、Java 17、MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0 |
| 反向代理 / 静态服务 | Nginx 1.27 |
| 编排 | Docker Compose |

---

## 目录结构

```
order-web/
├── docker-compose.yml          # 四个服务：mysql、backend、frontend、adminer
├── .env.example                # 环境变量样例
├── mysql/init/01-schema.sql    # 建表 + 演示数据（首次启动自动执行）
├── uploads/                    # 菜品图片（volume 持久化）
├── backend/
│   ├── Dockerfile              # 多阶段构建：Maven 编译 → JRE 运行
│   ├── pom.xml
│   └── src/main/
│       ├── resources/application.yml
│       └── java/com/example/ordering/
│           ├── OrderingApplication.java
│           ├── config/         # 静态资源、跨域、全局异常
│           ├── controller/     # Auth / Category / Dish / Order / Upload
│           ├── entity/         # Category / Dish / Orders / OrderItem
│           ├── mapper/         # MyBatis-Plus Mapper
│           ├── dto/            # 请求体与视图对象
│           └── exception/
└── frontend/
    ├── Dockerfile              # 多阶段构建：Node 打包 → Nginx 托管
    ├── nginx.conf              # 前端 + /api 反代 + /uploads 静态
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api.js              # 接口封装
        ├── router/index.js
        └── views/              # 入口页、菜单页、登录页、后台
```

---

## 快速开始（Docker）

### 1. 准备

服务器需要安装 Docker 与 Docker Compose：

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER && newgrp docker
docker --version && docker compose version
```

### 2. 启动

```bash
cd order-web
mkdir -p uploads            # 图片目录必须存在
cp .env.example .env        # 可选，修改数据库密码
docker compose up -d --build
```

首次构建会拉取镜像并编译前后端，约 5~15 分钟。

### 3. 访问

| 地址 | 用途 |
|---|---|
| `http://服务器IP:8080/` | 顾客入口，输入桌号进入 |
| `http://服务器IP:8080/admin/login` | 后台登录（`admin` / `admin123`） |
| `http://服务器IP:8082` | Adminer 数据库管理（Server `mysql`，用户 `root`，密码见 `.env`） |

> 云服务器需在安全组放行 `8080`；如需看数据库再放行 `8082`。

### 4. 常用命令

```bash
docker compose ps                 # 查看状态
docker compose logs -f backend    # 看后端日志
docker compose restart backend    # 重启后端
docker compose down               # 停止并删除容器（数据卷保留）
docker compose down -v            # 连数据库数据一起删除（重新初始化用）
```

---

## 本地开发（不用 Docker 也行）

### 后端

需要 JDK 17 + Maven，以及本地 MySQL（库名 `ordering`，执行 `mysql/init/01-schema.sql`）。

```bash
cd backend
mvn spring-boot:run
# 或：mvn clean package -DskipTests && java -jar target/app.jar
```

默认连接 `localhost:3306/ordering`，账号 `root` / `root123`，可用环境变量覆盖：

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/ordering?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root123
export UPLOAD_DIR=./uploads
```

### 前端

需要 Node 18+：

```bash
cd frontend
npm install
npm run dev     # http://localhost:5173，已配置代理到 8080
```

---

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 后台登录 |
| GET | `/api/categories` | 分类列表 |
| POST / PUT / DELETE | `/api/categories[/{id}]` | 分类增改删 |
| GET | `/api/dishes?categoryId=&availableOnly=true` | 菜品列表 |
| POST / PUT / DELETE | `/api/dishes[/{id}]` | 菜品增改删 |
| PATCH | `/api/dishes/{id}/toggle` | 上下架切换 |
| POST | `/api/orders` | 提交订单 |
| GET | `/api/orders?status=` | 订单列表 |
| GET | `/api/orders/{id}` | 订单详情 |
| PATCH | `/api/orders/{id}/status` | 修改订单状态 |
| POST | `/api/upload` | 上传菜品图片（multipart，字段名 `file`） |

订单提交请求体示例：

```json
{
  "tableId": "A01",
  "remark": "不要香菜",
  "items": [
    { "dishId": 1, "quantity": 2 },
    { "dishId": 8, "quantity": 1 }
  ]
}
```

> 价格由后端按数据库现价计算，前端传入的价格不参与结算。

---

## 数据持久化

- **数据库**：`mysql-data` 命名卷，`docker compose down` 不会删除。
- **图片**：宿主机 `./uploads` 同时挂载给后端（读写）和 Nginx（只读），容器重建不丢图。

如需重新初始化数据库：

```bash
docker compose down -v
docker compose up -d --build
```

---

## 常见问题

**端口 8080 被占用**
改 `docker-compose.yml` 中 frontend 的 `"8080:80"`，把宿主机端口换掉。

**图片上传成功但显示 404**
确认 `./uploads` 目录存在且已挂载到 frontend 容器（`docker exec ordering-frontend ls /app/uploads`）。

**后端启动报数据库连接失败**
MySQL 首次启动初始化需要时间，等健康检查通过后后端会自动重试；看 `docker compose logs mysql` 排查。

**修改了 SQL 但数据没变**
初始化脚本只在数据卷为空时执行一次，需 `docker compose down -v` 后重来。

**Nginx 502**
后端还没起好或被重建换了 IP。本项目 Nginx 已用 Docker 内置 DNS（`resolver 127.0.0.11`）动态解析，通常等待几秒即恢复；若持续 502，看 `docker compose logs backend`。

---

## 安全提示

> 本项目按「测试环境」设计：
> - 后台密码硬编码在 `AuthController` 中；
> - 管理接口没有 token 校验，知道接口就能改数据；
> - 数据库用 root 账号直连。
>
> 正式对外使用前，至少需要：后端加 Spring Security / JWT 鉴权、后台密码改存数据库并加密、数据库建独立低权限账号、启用 HTTPS。

## License

MIT
