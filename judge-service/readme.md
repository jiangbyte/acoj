## 运行

go build -o JudgeService main.go

docker run -itd --privileged --name ubuntu_golang --hostname ubuntu_golang ubuntu:latest

wget https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz

tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz

vi /etc/profile

export PATH=$PATH:/usr/local/go/bin

rm -rf /usr/local/go && tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz





---

## 1. 构建初始镜像

```bash
# 构建初始镜像
docker build -t my-go-app:initial .
```

## 2. 创建并运行容器

```bash
# 创建并运行容器（使用交互模式，特权模式）
docker run -it --privileged --name my-go-container my-go-app:initial
```

## 3. 在容器内进行修改

```bash
docker exec -it my-go-container /bin/bash
```

## 4. 基于修改后的容器创建新镜像

```bash
# 基于修改后的容器创建新镜像
docker commit my-go-container my-go-app:modified


docker commit my-ubuntu-02 registry.cn-beijing.aliyuncs.com/jiangbyte/ubuntu_sub_cgroup:1.0.0

```

## 5. 验证新镜像

```bash
# 查看新创建的镜像
docker images

# 测试新镜像
docker run -it --rm my-go-app:modified
```

---


cgroup v2 采用“**子树控制（subtree control）**”机制：父 cgroup 必须通过 `cgroup.subtree_control` 文件明确“下放”某个控制器的权限，子 cgroup 才能使用该控制器并生成对应的配置/统计文件（如 `memory.peak`、`cpu.max` 等）。

1. **父 cgroup 未启用 `memory` 控制器**：  
   父目录（通常是 `/sys/fs/cgroup`）的 `cgroup.subtree_control` 文件中，没有包含 `memory` 控制器，导致子 cgroup 无法使用内存相关功能，因此不会生成 `memory.peak`、`memory.max` 等文件。

2. **其他控制器（如 `cpu`、`io`）可能也未完全启用**：  
   从子 cgroup 的文件列表看，只有 `cpu.pressure`、`io.pressure` 等基础文件，缺少 `cpu.max`、`io.stat` 等，说明这些控制器也未在父级启用。


### 在父 cgroup 中启用控制器
要让子 cgroup 生成 `memory.peak` 等文件，需先在父 cgroup 中启用对应的控制器（以 `memory` 为例）：

#### 1. 查看父 cgroup 当前启用的控制器
```bash
# 查看根 cgroup（父级）的控制器状态
cat /sys/fs/cgroup/cgroup.subtree_control
```
- 输出为空或不含 `memory`、`cpu` 等，说明这些控制器未启用。


#### 2. 在父 cgroup 中启用所需控制器
通过 `cgroup.subtree_control` 文件添加控制器（需 root 权限）：
```bash
# 启用 memory 控制器（允许子 cgroup 使用内存相关功能）
sudo echo "+memory" | tee /sys/fs/cgroup/cgroup.subtree_control

# 同时启用 cpu、io 控制器（按需添加）
sudo echo "+cpu +io" | tee -a /sys/fs/cgroup/cgroup.subtree_control
```
- `+` 表示启用控制器，`-` 表示禁用。

---

### 清空父 cgroup 再修改
需先确保父 cgroup（`/sys/fs/cgroup`）中没有进程，且子 cgroup 未占用控制器，步骤如下：

#### 1. 迁移父 cgroup 中的进程到临时 cgroup
```bash
# 1. 创建一个临时 cgroup（用于临时存放进程）
mkdir /sys/fs/cgroup/temp

# 2. 将父 cgroup 中的所有进程迁移到临时 cgroup
# 注意：替换 $(cat /sys/fs/cgroup/cgroup.procs) 为实际 PID 列表
for pid in $(cat /sys/fs/cgroup/cgroup.procs); do
  echo $pid | tee /sys/fs/cgroup/temp/cgroup.procs
done

# 3. 确认父 cgroup 已无进程（输出为空）
cat /sys/fs/cgroup/cgroup.procs
```

#### 3. 再次尝试启用父 cgroup 的控制器
```bash
# 启用 memory 控制器（允许子 cgroup 使用内存相关功能）
sudo echo "+memory" | tee /sys/fs/cgroup/cgroup.subtree_control

# 同时启用 cpu、io 控制器（按需添加）
sudo echo "+cpu +io" | tee -a /sys/fs/cgroup/cgroup.subtree_control
```

#### 4. 恢复进程
若需将临时 cgroup 中的进程迁回父 cgroup：
```bash
for pid in $(cat /sys/fs/cgroup/temp/cgroup.procs); do
  echo $pid | tee /sys/fs/cgroup/cgroup.procs
done

# 删除临时 cgroup
rmdir /sys/fs/cgroup/temp
```






