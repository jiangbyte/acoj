```bash
sudo vim /etc/systemd/system/vue-admin-server.service
```

内容如下

```ini
[Unit]
Description=Vue.js Server
After=network.target

[Service]
User=root
WorkingDirectory=/root/dev/admin/dist
ExecStart=/usr/local/bin/serve -s . -l 3010
Restart=always

[Install]
WantedBy=multi-user.target
```


```bash
sudo systemctl daemon-reload
sudo systemctl start vue-admin-server
sudo systemctl enable vue-admin-server

sudo systemctl restart vue-admin-server
sudo systemctl stop vue-admin-server

sudo journalctl -u vue-admin-server -f
```