# 本地 / 协作开发启动

两个环境的含义：

| 环境 | 代码位置 | 基础设施 | 引导配置 | 是否进 Git |
|------|----------|----------|----------|------------|
| **local** | 本机 | **本机** MySQL / Redis / Nacos / MQ 等 | `*-local*` | 是 |
| **dev** | 本机 | **共同远程** 开发机上的中间件 | `*-dev*` | 否（gitignore） |

- **local**：本机自测，Nacos 与库表都在本机，地址用 `localhost`。
- **dev**：多人共用同一套远程 MySQL / Redis / Nacos / MQ；本机只跑业务进程。引导文件里的 Nacos 地址填**共享开发机**；Nacos 里下发的数据源等也必须指向**同一台远程机**（不要写成 `localhost`，否则会连到开发者自己电脑）。

公网 IP / 主机名只写在本机私有的 `*-dev.yaml`（不提交）和远程 Nacos 控制台里，**不要写进仓库文档或可提交配置**。

## 前置（dev）

1. 从模板复制私有引导（仅首次），把其中的 Nacos `server-addr` / `ServerAddr` 改成团队的共享开发机：

```bash
cp service-system/src/main/resources/application-dev.yaml.example \
   service-system/src/main/resources/application-dev.yaml

cp service-judge/etc/judge-dev.yaml.example service-judge/etc/judge-dev.yaml
cp service-similarity/etc/similar-dev.yaml.example service-similarity/etc/similar-dev.yaml
```

2. 确认远程 Nacos 已发布 `common.yaml`、`service-system.yaml`、`service-judge.yaml`、`service-similarity.yaml`，且其中 MySQL / Redis / RabbitMQ / 对象存储主机为**远程地址**（与引导里的开发机一致）。

3. `service-system` 的 Nacos 地址与 `spring.config.import` 必须写在同一个 `application-{profile}.yaml` 里（已按此组织）；不要只改主 `application.yaml`。

## system

在 `service-system` 目录执行（根目录 `-pl` 可能解析不到 `spring-boot` 插件）：

```bash
cd service-system

# local：本机基础设施（默认 -Plocal）
mvn spring-boot:run

# dev：本机代码 + 共同远程基础设施（需 application-dev.yaml）
mvn spring-boot:run -Pdev
```

默认端口 `89` 在 Linux 上常因非 root 无法绑定；本机可：

```bash
mvn spring-boot:run -Pdev -Dspring-boot.run.arguments='--server.port=8089'
```

## judge / similarity

```bash
# judge
cd service-judge
go run main.go -f etc/judge-local.yaml -nacos   # local
go run main.go -f etc/judge-dev.yaml -nacos     # dev

# similarity
cd service-similarity
go run main.go -f etc/similar-local.yaml -nacos # local
go run main.go -f etc/similar-dev.yaml -nacos   # dev
```

默认 `-f` 是 `*-local.yaml`；协作开发请显式用 `*-dev.yaml`。

## 配置文件对照

| 服务 | local（可提交） | dev（gitignore） | 模板 |
|------|-----------------|------------------|------|
| service-system | `application-local.yaml` | `application-dev.yaml` | `application-dev.yaml.example` |
| service-judge | `etc/judge-local.yaml` | `etc/judge-dev.yaml` | `etc/judge-dev.yaml.example` |
| service-similarity | `etc/similar-local.yaml` | `etc/similar-dev.yaml` | `etc/similar-dev.yaml.example` |

## shared-dev 业务数据

共享 MySQL（Nacos `common.yaml` 中的 `astro_code`）的题库 / 题集 / 竞赛 / 普通用户 / 样本库 / 提交数据，按「线上导出 → 正规化」维护：

1. 从 shared-dev 导出相关表（备份放在仓库外，例如 `~/Downloads/acoj-dev-export-YYYYMMDD/`）。
2. 用 [`sql/scripts/gen_dev_formal_dataset.py`](../sql/scripts/gen_dev_formal_dataset.py) 生成雪花 ID、去掉 Seed/demo/junit 痕迹的 patch。
3. 将 [`sql/patches/20261121_dev_formal_dataset.sql`](../sql/patches/20261121_dev_formal_dataset.sql) 写回同一库。

**禁止**对共享库执行 [`deploy/docker/scripts/seed-demo.sh`](../deploy/docker/scripts/seed-demo.sh) 或导入 `deploy/docker/sql/01-judge-demo.sql` / `02-similarity-demo.sql`。那些脚本仅限本机 Docker Compose 库做联调。

系统账号 `0` / `1` / `2`（default / SuperAdmin / Admin）与菜单、字典、角色配置不在上述 patch 覆盖范围内。

正规化后若出现“题集/竞赛有题但无提交、AC 无样本”等断链，再跑闭环补齐：

```bash
python3 sql/scripts/fill_dev_closed_loop.py --apply
```

该脚本保证：每位普通用户都有 PROBLEM / SET / CONTEST 的 Accepted 记录与样本库；每场竞赛都有报名；每道题集题、竞赛题至少有一条对应模块提交；AC 组均有 `data_library` / `data_solved`（题集另有 `data_progress`）。
