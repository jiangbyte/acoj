# 快速开始

本指南将带你 5 分钟内启动 Hei FastAPI 项目。

## 环境要求

在开始之前，请确保你的开发环境满足以下要求：

| 依赖 | 版本要求 |
|------|---------|
| Python | 3.10 或更高版本 |
| MySQL | 8.0 或更高版本 |
| Redis | 6.0 或更高版本 |

## 第一步：克隆项目

```bash
git clone <项目仓库地址>
cd hei-fastapi
```

## 第二步：安装依赖

推荐使用虚拟环境：

```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 或
venv\Scripts\activate     # Windows

pip install -r requirements.txt
```

## 第三步：初始化数据库

在 MySQL 中创建数据库并导入 DDL：

```bash
mysql -u root -p < scripts/sqls/hei_ddl.sql
```

或手动创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS `hei_data` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 第四步：修改配置

编辑项目根目录下的 `.env` 文件，根据你的本地环境修改数据库和 Redis 连接信息：

```env
DB__HOST=localhost
DB__PORT=3306
DB__USER=root
DB__PASSWORD=123456
DB__DATABASE=hei_data

REDIS__HOST=localhost
REDIS__PORT=6379
REDIS__PASSWORD=123456

SM2__PRIVATE_KEY=your-sm2-private-key
SM2__PUBLIC_KEY=your-sm2-public-key

TOKEN__EXPIRE_SECONDS=2592000
TOKEN__TOKEN_NAME=Authorization
```

默认配置中包含了开发环境使用的账号密码，请务必在部署前修改。

## 第五步：运行项目

```bash
python main.py
```

启动成功的输出类似：

```
INFO:     Started server process [12345]
INFO:     Waiting for application startup.
INFO:     MySQL connected successfully
INFO:     Redis connected successfully
INFO:     Permission scan completed
INFO:     Application startup complete.
INFO:     Uvicorn running on http://127.0.0.1:18885
```

## 第六步：验证启动

服务启动后，访问健康检查接口验证服务是否正常运行：

```bash
curl http://localhost:18885/
```

预期响应：

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "message": "hei-fastapi is running",
    "version": "1.0.0"
  },
  "success": true,
  "trace_id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

同时可访问自动生成的 API 文档：

- **Swagger UI**：<http://localhost:18885/docs>
- **ReDoc**：<http://localhost:18885/redoc>

## 默认账号

| 端侧 | 账号 | 密码 |
|------|------|------|
| B 端管理 | admin | admin123（需 SM2 加密传输）|
| C 端用户 | 需自行注册 | 需自行注册 |

> 注意：登录密码需要通过 SM2 公钥加密后传输，具体流程请参考 [认证模块](../features/auth)。

## 下一步

- 了解 [配置文件说明](config) 中的详细配置项
- 阅读 [架构概述](../architecture/overview) 理解框架设计
- 参考 [模块开发](../modules/development) 开始业务功能开发
