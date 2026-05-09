# Git 两个独立仓库长期合并文档

## 仓库信息

- **framework 仓库（基础框架）**：`git@github.com:jiangbyte/hei-fastapi.git`
- **业务仓库（业务项目）**：基于 framework 仓库开发的业务代码仓库
- **分支名称**：两个仓库的主分支均为 `main`

## 前置条件

- 已安装 Git
- 拥有业务仓库的本地副本
- 拥有 framework 仓库的读取权限
- 两个仓库没有共同的提交历史

## 操作步骤

### 第一步：进入业务仓库的本地目录

```bash
cd /path/to/业务仓库
```

### 第二步：添加 framework 仓库作为远程仓库

```bash
git remote add framework git@github.com:jiangbyte/hei-fastapi.git
```

### 第三步：拉取 framework 仓库的所有分支和提交历史

```bash
git fetch framework
```

### 第四步：切换到业务仓库的 main 分支

```bash
git checkout main
```

### 第五步：执行首次合并

```bash
git merge framework/main --allow-unrelated-histories
```

### 第六步：处理合并冲突

查看冲突文件列表：

```bash
git status
```

对于每个冲突文件，手动编辑，删除以下标记：

```
<<<<<<<
=======
>>>>>>>
```

编辑完成后：

```bash
git add .
```

### 第七步：提交合并结果

```bash
git commit -m "合并 framework 基础框架"
```

### 第八步：推送合并后的代码到远程业务仓库

```bash
git push origin main
```

## 后续每次 framework 仓库升级时的操作

### 步骤 1：进入业务仓库目录

```bash
cd /path/to/业务仓库
```

### 步骤 2：拉取 framework 仓库的最新代码

```bash
git fetch framework
```

### 步骤 3：合并 framework 仓库的更新到业务仓库

```bash
git merge framework/main
```

### 步骤 4：解决冲突（如果有）

```bash
git status
```

手动编辑冲突文件：

```bash
git add .
git commit -m "同步 framework 最新更新"
```

### 步骤 5：推送到远程业务仓库

```bash
git push origin main
```

## 常用命令速查

| 操作 | 命令 |
|------|------|
| 查看远程仓库列表 | `git remote -v` |
| 拉取 framework 更新 | `git fetch framework` |
| 合并 framework 到当前分支 | `git merge framework/main` |
| 查看冲突文件 | `git status` |
| 放弃合并 | `git merge --abort` |
| 删除远程仓库引用 | `git remote remove framework` |
| 重命名远程仓库 | `git remote rename framework upstream` |

## 完整脚本示例

创建文件 `sync.sh`：

```bash
#!/bin/bash
cd /path/to/业务仓库
git fetch framework
git merge framework/main
```

执行脚本：

```bash
chmod +x sync.sh
./sync.sh
```

## 注意事项

- 首次合并必须使用 `--allow-unrelated-histories` 参数
- 后续合并不需要该参数
- 合并前确保业务仓库的工作区没有未提交的修改
- 合并完成后需要推送到远程业务仓库才能生效