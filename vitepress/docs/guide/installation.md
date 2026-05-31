# 安装配置

本页详细说明 Hei FastAPI 框架的环境要求和完整安装步骤。

## 环境要求

### Python 环境

Hei FastAPI 需要 **Python 3.10 或更高版本**。安装步骤如下：

1. **下载 Python**：访问 [Python 官方下载页面](https://www.python.org/downloads/) 下载对应操作系统的安装包
2. **安装 Python**：按照官方指引完成安装（Windows 用户请勾选"Add Python to PATH"）
3. **验证安装**：

```bash
python --version
# 输出示例：Python 3.12.1
```

4. **配置 pip 镜像**（可选，国内推荐）：

```bash
pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
```

### MySQL 数据库

需要 **MySQL 8.0 或更高版本**，需要支持 utf8mb4 字符集。

1. **安装 MySQL**：可从 [MySQL 官方下载](https://dev.mysql.com/downloads/) 获取
2. **创建数据库**：

```sql
CREATE DATABASE IF NOT EXISTS `hei_data` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

3. **导入 DDL**：

```bash
mysql -u root -p hei_data < scripts/sqls/hei_ddl.sql
```

### Redis 缓存

需要 **Redis 6.0 或更高版本**，用于：

- Token 会话存储
- 图形验证码存储
- 权限缓存
- 防重复提交令牌

1. **安装 Redis**：可从 [Redis 官方下载](https://redis.io/download/) 获取
2. **Windows 用户**：Redis 官方不支持 Windows，建议使用 WSL2 或 Docker
3. **使用 Docker**：

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

4. **验证连接**：

```bash
redis-cli ping
# 输出：PONG
```

## 完整安装步骤

### 步骤 1：获取项目代码

```bash
git clone <项目仓库地址>
cd hei-fastapi
```

### 步骤 2：创建虚拟环境并安装依赖

```bash
# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Linux/Mac:
source venv/bin/activate
# Windows:
# venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt
```

### 步骤 3：配置 .env 文件

将项目根目录的 `.env` 文件修改为你的本地环境配置。详细配置项说明请参考 [配置文件说明](config)。

### 步骤 4：初始化数据库

```bash
mysql -u root -p < scripts/sqls/hei_ddl.sql
```

### 步骤 5：启动服务

```bash
python main.py
```

开发模式下推荐使用热重载：

```bash
# 使用 uvicorn 直接启动（带热重载）
uvicorn main:app --host 127.0.0.1 --port 18885 --reload
```

### 步骤 6：验证部署

访问以下接口验证服务正常运行：

- 健康检查：`GET http://localhost:18885/`
- API 文档：`GET http://localhost:18885/docs`
- B 端获取验证码：`GET http://localhost:18885/api/v1/public/b/captcha`
- B 端获取 SM2 公钥：`GET http://localhost:18885/api/v1/public/b/sm2/public-key`

## 生产环境部署

### 使用 Uvicorn 直接启动

```bash
uvicorn main:app --host 0.0.0.0 --port 18885 --workers 4
```

### 使用 Gunicorn + Uvicorn（Linux）

```bash
pip install gunicorn
gunicorn main:app --worker-class uvicorn.workers.UvicornWorker --bind 0.0.0.0:18885 --workers 4
```

### 使用 Systemd 管理（Linux）

```ini
[Unit]
Description=Hei FastAPI Service
After=network.target

[Service]
Type=simple
User=www
WorkingDirectory=/var/www/hei-fastapi
ExecStart=/var/www/hei-fastapi/venv/bin/uvicorn main:app --host 0.0.0.0 --port 18885
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

### 使用 Docker 部署

参考项目根目录下的 Dockerfile：

```dockerfile
FROM python:3.12-slim AS builder
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

FROM python:3.12-slim
WORKDIR /app
COPY --from=builder /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY . .
EXPOSE 18885
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "18885"]
```

## 常见问题

**Q: 启动报错 "ModuleNotFoundError"**

A：请确认已激活虚拟环境并执行了 `pip install -r requirements.txt`。

**Q: 数据库连接失败**

A：确认 MySQL 服务已启动，且 `.env` 中的连接信息正确。

**Q: Redis 连接失败**

A：确认 Redis 服务已启动，端口和密码配置正确。

**Q: SM2 密钥配置错误**

A：SM2 密钥对需要正确配置，可通过 `/api/v1/public/b/sm2/public-key` 接口验证公钥是否正确返回。
