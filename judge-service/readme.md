## 运行

go build -o JudgeService main.go

docker run -itd --privileged --name ubuntu_golang --hostname ubuntu_golang ubuntu:latest

wget https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz

tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz

vi /etc/profile

export PATH=$PATH:/usr/local/go/bin

rm -rf /usr/local/go && tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz