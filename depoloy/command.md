使用 systemd 服务

```bash
sudo vim /etc/systemd/system/app-core.service
```

内容如下

```ini
[Unit]
Description=App Core Spring Boot Service
After=network.target

[Service]
User=root
WorkingDirectory=/root/dev/
ExecStart=/usr/bin/java -jar /root/dev/app-core-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=30

[Install]
WantedBy=multi-user.target
```


启用并启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable app-core
sudo systemctl start app-core
```

update-app.sh：

```bash
#!/bin/bash
SERVICE_NAME="app-core"
JAR_PATH="/root/dev/app-core-1.0.0.jar"
BACKUP_DIR="/root/dev/backups"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 停止服务
echo "Stopping service..."
sudo systemctl stop $SERVICE_NAME

# 备份旧版本
if [ -f "$JAR_PATH" ]; then
    backup_name="app-core-$(date +%Y%m%d-%H%M%S).jar"
    mv "$JAR_PATH" "$BACKUP_DIR/$backup_name"
    echo "Backup created: $BACKUP_DIR/$backup_name"
fi

NEW_JAR="/root/dev/app-core-new.jar"
if [ -f "$NEW_JAR" ]; then
    mv "$NEW_JAR" "$JAR_PATH"
    echo "New version deployed"
fi

# 启动服务
echo "Starting service..."
sudo systemctl start $SERVICE_NAME

# 查看状态
sudo systemctl status $SERVICE_NAME
```

查看 Spring Boot 服务状态：

sudo systemctl status app-core

查看日志：

sudo journalctl -u app-core -f