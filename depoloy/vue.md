sudo npm install -g pm2

使用 serve 包

npm install -g serve

运行 dist 目录

serve -s dist -l 3010


使用 systemd 服务

```bash
sudo vim /etc/systemd/system/vue-server.service
```

内容如下

```ini
[Unit]
Description=Vue.js Server
After=network.target

[Service]
User=root
WorkingDirectory=/root/dev/dist
ExecStart=/usr/local/bin/serve -s . -l 3010
Restart=always

[Install]
WantedBy=multi-user.target
```


启用并启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl start vue-server
sudo systemctl enable vue-server
```
