# Admin-Vue 子项目管理（Git Subtree）

项目使用 Git Subtree 将 [hei-admin-vue](https://github.com/jiangbyte/hei-admin-vue) 作为子项目集成到 `admin-vue/` 目录，便于同步上游框架的更新。

## 首次克隆

```bash
git clone git@github.com:jiangbyte/acoj.git
cd acoj
```

Subtree 的代码已包含在主仓库中，无需额外命令，`admin-vue/` 目录立即可用。

## 拉取上游更新

当 hei-admin-vue 框架有更新时，通过 subtree pull 同步：

```bash
git subtree pull --prefix=admin-vue git@github.com:jiangbyte/hei-admin-vue.git main
```

如果有冲突，解决后正常 commit 即可。

## 本地修改后合并上游

如果 `admin-vue/` 目录下的代码有本地修改，pull 时会自动合并。流程不变：

```bash
# 先提交本地修改
git add admin-vue
git commit -m "feat: 修改 admin-vue 配置"

# 再拉取上游更新
git subtree pull --prefix=admin-vue git@github.com:jiangbyte/hei-admin-vue.git main
```

## 向 upstream 推送修改

如果对 `admin-vue/` 的修改也想贡献回 hei-admin-vue 框架：

```bash
git subtree push --prefix=admin-vue git@github.com:jiangbyte/hei-admin-vue.git main
```

需要拥有 hei-admin-vue 仓库的写入权限。

## 工作原理

- Subtree 将外部仓库的历史完整复制到主仓库的 `admin-vue/` 子目录
- `git log` 中可以看到 hei-admin-vue 的所有提交历史
- 主仓库的普通 `git pull` / `git push` 不受影响
- 无需 `.gitmodules` 文件，clone 后立即可用

## 常见问题

### Q: 忘记 subtree 语法了怎么办？
A: 项目中已配置远程仓库别名，可以简化命令（尚未配置时请先执行一次）：

```bash
# 查看远程仓库
git remote -v
```

### Q: subtree pull 冲突了如何解决？
A: 和普通 git merge 冲突解决方式完全一致：
1. 编辑冲突文件
2. `git add` 标记已解决
3. `git commit` 完成合并
