sudo systemctl stop app-core
sudo systemctl stop vue-server

sudo systemctl restart app-core
sudo systemctl restart vue-server
sudo journalctl -u app-core -f