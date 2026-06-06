# plugin-judge 评测插件实现计划

> 基于 `plugin-sys` / `plugin-client` 的模块化模式，为 acoj 系统提供完整的评测功能。

---

## 一、模块结构总览

```
plugin-judge/
├── plugin.go                 # 插件入口，定义 JudgePlugin，注册 module
├── imports.go                # blank-import 所有子模块 init()
│
├── sandbox/                  # 沙箱抽象层（可扩展）
│   ├── types.go              #   SandboxBackend 接口 + ExecRequest / ExecResult / HealthStatus
│   ├── health.go             #   健康检查统一逻辑
│   └── gojudge/              #   go-judge 适配器
│       ├── adapter.go        #     SandboxBackend 实现
│       └── client.go         #     go-judge HTTP 通信
│
├── judge/                    # 判题引擎
│   ├── model.go              #   JudgeConfig GORM 模型
│   ├── migrate.go            #   注册模型
│   ├── params.go             #   VO / 参数
│   ├── engine.go             #   判题核心调度：根据 judge_type 分发
│   ├── spj.go                #   Special Judge：编译 + 运行自定义校验器
│   ├── interactive.go        #   交互式判题：用户程序 ↔ 交互器双向通信
│   └── api/v1/api.go         #   CRUD 路由
│
├── problem/                  # 题目
│   ├── model.go
│   ├── migrate.go
│   ├── params.go
│   ├── service.go            #   CRUD + 搜索 + N+1 防护
│   └── api/v1/api.go
│
├── testcase/                 # 测试用例
│   ├── model.go
│   ├── migrate.go
│   ├── params.go
│   ├── service.go            #   CRUD + 文件上传
│   └── api/v1/api.go
│
├── submission/               # 提交 + 判题队列
│   ├── model.go
│   ├── migrate.go
│   ├── params.go
│   ├── service.go            #   提交 → 入队 → 回调更新
│   ├── queue.go              #   Redis List 判题队列（LPUSH / BRPOP）
│   └── api/v1/api.go
│
├── contest/                  # 竞赛
│   ├── model.go
│   ├── migrate.go
│   ├── params.go
│   ├── service.go            #   CRUD + 报名
│   ├── rank.go               #   排行榜（内存聚合，防 N+1）
│   └── api/v1/api.go
│
├── problemset/               # 题单
│   ├── model.go
│   ├── migrate.go
│   ├── params.go
│   ├── service.go
│   └── api/v1/api.go
│
└── tag/                      # 标签
    ├── model.go
    ├── migrate.go
    ├── params.go
    ├── service.go
    └── api/v1/api.go
```

---

## 二、沙箱抽象层（sandbox/）

### 2.1 SandboxBackend 接口

```go
package sandbox

// ExecRequest 单次执行请求
type ExecRequest struct {
    Code       string            // 源代码
    Language   string            // c/cpp/python/java/go/rust
    Stdin      string            // 标准输入
    MaxCPUTime int64             // CPU 时间限制（ns）
    MaxRealTime int64            // 实际时间限制（ns）
    MaxMemory  int64             // 内存限制（byte）
    MaxStack   int64             // 栈限制（byte）
    MaxOutput  int64             // 输出限制（byte）
    Env        []string          // 环境变量
}

// ExecResult 单次执行结果
type ExecResult struct {
    Status     string    // Accepted / CompileError / TLE / MLE / RE / SE
    ExitCode   int
    TimeUsed   int64     // ns
    MemoryUsed int64     // byte
    Stdout     string
    Stderr     string
    Error      string    // 沙箱内部错误
}

// HealthStatus 健康状态
type HealthStatus struct {
    Alive       bool
    Version     string
    BackendName string
    Error       string
}

// SandboxBackend 沙箱后端接口
type SandboxBackend interface {
    Name() string
    Exec(req *ExecRequest) (*ExecResult, error)
    BatchExec(reqs []*ExecRequest) ([]*ExecResult, error)
    Health() *HealthStatus
}
```

### 2.2 健康检查

- **定时检测**：`JudgePlugin.Start()` 中启动 goroutine，每 `judge.health_check_interval` 秒对所有后端执行 `Health()`
- **懒检测**：首次 `Exec()` 前自动检测，不健康则拒绝提交
- **多实例**：支持配置多个 go-judge 实例，各自独立检测，不健康自动摘除
- **管理 API**：

  ```
  GET /api/v1/judge/sandbox/health
  → [{backend: "go-judge", endpoint: "localhost:5051", alive: true, version: "1.0", error: ""}]
  ```

### 2.3 go-judge 适配器

- 通过 HTTP 调用 go-judge `/run` 接口执行代码
- 将 go-judge 的返回状态映射到内部 `ExecResult.Status`
- 支持 `BatchExec`：并发调用多个 `/run` 请求

### 2.4 可扩展性

后续新增沙箱只需：

```go
type DockerBackend struct { ... }
func (d *DockerBackend) Name() string { return "docker" }
func (d *DockerBackend) Exec(req *ExecRequest) (*ExecResult, error) { ... }
// ...

// 在配置中增加:
// sandbox:
//   backends:
//     - name: docker
//       endpoint: unix:///var/run/docker.sock
```

---

## 三、判题引擎（judge/）

### 3.1 判题方式

| JudgeType | 说明 |
|---|---|
| `standard` | 标准对比：运行用户程序，逐字节对比输出（忽略行末空白） |
| `spj` | Special Judge：先编译 SPJ 程序，将用户输出和标准答案传入 SPJ，以 SPJ 退出码和输出判定 |
| `interactive` | 交互式：用户程序与交互器通过 stdin/stdout 双向通信，以交互器结果判定 |
| `answer_submit` | 答案提交：直接比对提交的答案文件与标准答案 |

### 3.2 评测状态机

```
PENDING → JUDGING → ACCEPTED
                  → WRONG_ANSWER
                  → TIME_LIMIT_EXCEEDED
                  → MEMORY_LIMIT_EXCEEDED
                  → RUNTIME_ERROR
                  → COMPILE_ERROR
                  → SYSTEM_ERROR
```

### 3.3 子任务（Subtask）

JudgeConfig 的 `subtasks` 字段存储 JSON：

```json
[
  {"id": 1, "score": 30, "testcase_ids": ["t1","t2"], "depends_on": []},
  {"id": 2, "score": 30, "testcase_ids": ["t3","t4"], "depends_on": [1]},
  {"id": 3, "score": 40, "testcase_ids": ["t5","t6"], "depends_on": [1,2]}
]
```

- 依赖子任务失败 → 跳过不评测
- 子任务内全部测试点通过 → 获得该子任务分数

---

## 四、判题队列（submission/queue.go）

### 4.1 Redis List 方案

```
┌─────────┐  LPUSH   ┌──────────────┐  BRPOP   ┌────────────┐  HTTP   ┌──────────┐
│  API    │ ──────→  │  Redis List  │ ←──────  │   Worker   │ ─────→ │ go-judge │
│  Submit │          │ judge:queue  │          │ (goroutine) │ ←───── │          │
└─────────┘          └──────────────┘          └──────┬──────┘        └──────────┘
                                                       │
                                                       ↓
                                                ┌──────────────┐
                                                │  DB: Update   │
                                                │  Submission   │
                                                │  Status/Result│
                                                └──────────────┘
```

- **入队**：`LPUSH judge:queue submission_id`
- **出队**：Worker 循环 `BRPOP judge:queue 0` 阻塞获取
- **并发控制**：启动 N 个 Worker（`judge.concurrent`），每个独立 BRPOP
- **可靠消费**：Worker 从队列取出 submission_id 后先检查数据库中状态是否为 `pending`，避免重复消费
- **失败重试**：判题失败（非 SE）不重新入队，直接写入结果；SE 可配置重试次数

### 4.2 Worker 生命周期

```go
func (p *JudgePlugin) Start() error {
    for i := 0; i < p.concurrent; i++ {
        go p.runWorker(i)
    }
    go p.healthCheckLoop()
    return nil
}

func (p *JudgePlugin) Stop() error {
    close(p.stopCh) // 通知 Worker 退出
    p.wg.Wait()     // 等待所有 Worker 完成
    return nil
}
```

---

## 五、N+1 查询防护策略

| 场景 | 问题 | 方案 |
|---|---|---|
| Problem 分页 + 显示标签名 | 每页 20 条 → 20次标签查询 | 查完 problem 列表后收集所有 problem_id → `WHERE problem_id IN (...)` 一次查出关联 → Go 内存 map 聚合 |
| Contest 详情 + 题目列表 | 展示时逐条查 problem | problem_ids 存 JSON 字段，解析后用 `WHERE id IN (...)` 批量查 |
| Contest Rank | 每个用户每道题逐条查 submission | 一次查出该比赛全部 submission，内存中按 user+problem 分组算排名 |
| Submission 列表 + 显示用户名/题名 | 每页逐条联表 | 列表用 JOIN 一次查出 user.name + problem.title，或用两次批量查后内存聚合 |
| ProblemSet 详情 + 题目 | 解析 JSON 后逐条查 | `WHERE id IN (...)` 批量取出 |

---

## 六、数据模型定义

### 6.1 标签（tag）

```go
type JudgeTag struct {
    ID    string `gorm:"primaryKey;size:32"`
    Name  string `gorm:"size:64;not null;uniqueIndex"`
    Color string `gorm:"size:16;default:#1890ff"`
}

func (JudgeTag) TableName() string { return "judge_tag" }
```

### 6.2 题目（problem）

```go
type JudgeProblem struct {
    ID              string  `gorm:"primaryKey;size:32"`
    Title           string  `gorm:"size:255;not null"`
    Content         string  `gorm:"type:text;not null"`   // Markdown
    Difficulty      string  `gorm:"size:16;default:easy"` // easy/medium/hard/expert
    DefaultMaxCPU   int64   `gorm:"default:10000000000"`  // 10s (ns)
    DefaultMaxMemory int64  `gorm:"default:268435456"`    // 256MB (byte)
    JudgeConfigID   *string `gorm:"size:32"`
    Source          *string `gorm:"size:255"`
    Visibility      string  `gorm:"size:16;default:public"` // public/private/internal
    CreatedAt       time.Time
    CreatedBy       string  `gorm:"size:32"`
    UpdatedAt       *time.Time
    UpdatedBy       *string `gorm:"size:32"`
}

// 题目-标签 多对多中间表
type JudgeProblemTag struct {
    ProblemID string `gorm:"primaryKey;size:32"`
    TagID     string `gorm:"primaryKey;size:32"`
}

func (JudgeProblem) TableName() string { return "judge_problem" }
func (JudgeProblemTag) TableName() string { return "judge_problem_tag" }
```

### 6.3 测试用例（testcase）

```go
type JudgeTestCase struct {
    ID         string  `gorm:"primaryKey;size:32"`
    ProblemID  string  `gorm:"size:32;not null;index"`
    Type       string  `gorm:"size:16;default:test"` // sample / pretest / test
    Input      string  `gorm:"type:text"`             // 文本内容或文件引用
    Output     string  `gorm:"type:text"`             // 文本内容或文件引用
    InputFile  *string `gorm:"size:500"`              // 文件存储路径
    OutputFile *string `gorm:"size:500"`              // 文件存储路径
    Score      float64 `gorm:"default:0"`
    SortOrder  int     `gorm:"default:0"`
    CreatedAt  time.Time
}

func (JudgeTestCase) TableName() string { return "judge_testcase" }
```

### 6.4 判题配置（judge config）

```go
type JudgeConfig struct {
    ID           string  `gorm:"primaryKey;size:32"`
    ProblemID    string  `gorm:"size:32;uniqueIndex;not null"`
    JudgeType    string  `gorm:"size:20;default:standard"` // standard/spj/interactive/answer_submit
    MaxCPUTime   int64   `gorm:"default:10000000000"`      // ns
    MaxRealTime  int64   `gorm:"default:30000000000"`      // ns
    MaxMemory    int64   `gorm:"default:268435456"`         // byte
    MaxStack     int64   `gorm:"default:67108864"`          // byte
    MaxOutput    int64   `gorm:"default:67108864"`          // byte
    Languages    string  `gorm:"type:text"`                  // JSON: ["c","cpp","python3","java","go","rust"]
    Subtasks     *string `gorm:"type:text"`                  // JSON: 子任务配置
    SpjCode      *string `gorm:"type:text"`                  // SPJ 源码
    SpjLanguage  *string `gorm:"size:20"`                    // SPJ 语言
    CreatedAt    time.Time
    UpdatedAt    *time.Time
}

func (JudgeConfig) TableName() string { return "judge_config" }
```

### 6.5 提交（submission）

```go
type JudgeSubmission struct {
    ID         string    `gorm:"primaryKey;size:32"`
    ProblemID  string    `gorm:"size:32;not null;index"`
    UserID     string    `gorm:"size:32;not null;index"`
    ContestID  *string   `gorm:"size:32;index"`
    Code       string    `gorm:"type:text;not null"`
    Language   string    `gorm:"size:20;not null"`
    Status     string    `gorm:"size:20;default:pending;index"` // 状态机值
    Score      float64   `gorm:"default:0"`
    TimeUsed   *int64    `gorm:"default:null"`
    MemoryUsed *int64    `gorm:"default:null"`
    Detail     *string   `gorm:"type:text"`                       // JSON: 每个测试点结果
    Message    *string   `gorm:"type:text"`                       // 编译错误或系统错误信息
    CreatedAt  time.Time `gorm:"index"`
}

func (JudgeSubmission) TableName() string { return "judge_submission" }
```

### 6.6 竞赛（contest）

```go
type JudgeContest struct {
    ID            string     `gorm:"primaryKey;size:32"`
    Title         string     `gorm:"size:255;not null"`
    Type          string     `gorm:"size:16;not null"` // acm / oi / ioi
    Description   *string    `gorm:"type:text"`
    StartTime     time.Time  `gorm:"not null;index"`
    EndTime       time.Time  `gorm:"not null"`
    FreezeMinutes *int       `gorm:"default:null"`     // 封榜前分钟
    ProblemIDs    string     `gorm:"type:text;not null"` // JSON: [{id, score, sort_order}]
    Visibility    string     `gorm:"size:16;default:public"` // public / private
    Password      *string    `gorm:"size:64"`           // 参赛密码
    CreatedBy     string     `gorm:"size:32;not null"`
    CreatedAt     time.Time
    UpdatedAt     *time.Time
}

type JudgeContestRegister struct {
    ContestID string    `gorm:"primaryKey;size:32"`
    UserID    string    `gorm:"primaryKey;size:32"`
    CreatedAt time.Time
}

func (JudgeContest) TableName() string { return "judge_contest" }
func (JudgeContestRegister) TableName() string { return "judge_contest_register" }
```

### 6.7 题单（problemset）

```go
type JudgeProblemSet struct {
    ID          string     `gorm:"primaryKey;size:32"`
    Title       string     `gorm:"size:255;not null"`
    Description *string    `gorm:"type:text"`
    ProblemIDs  string     `gorm:"type:text;not null"` // JSON: [{id, sort_order}]
    Visibility  string     `gorm:"size:16;default:public"`
    CreatedBy   string     `gorm:"size:32;not null"`
    CreatedAt   time.Time
    UpdatedAt   *time.Time
}

func (JudgeProblemSet) TableName() string { return "judge_problemset" }
```

---

## 七、完整路由表

### 7.1 沙箱健康检查

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/sandbox/health` | - | 所有沙箱后端健康状态 |

### 7.2 标签

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/tag/page` | `current, size` | 标签分页 |
| POST | `/api/v1/judge/tag/create` | `{name, color}` | 创建标签 |
| POST | `/api/v1/judge/tag/modify` | `{id, name, color}` | 编辑标签 |
| POST | `/api/v1/judge/tag/remove` | `{ids:[]}` | 删除标签 |
| GET | `/api/v1/judge/tag/detail` | `id` | 标签详情 |
| GET | `/api/v1/judge/tag/options` | - | 标签选项列表 |

### 7.3 题目

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/problem/page` | `current, size, keyword, difficulty, tag_id` | 题目分页（支持按关键词/难度/标签筛选） |
| POST | `/api/v1/judge/problem/create` | `{title, content, difficulty, ...}` | 创建题目 |
| POST | `/api/v1/judge/problem/modify` | `{id, title, content, ...}` | 编辑题目 |
| POST | `/api/v1/judge/problem/remove` | `{ids:[]}` | 删除题目 |
| GET | `/api/v1/judge/problem/detail` | `id` | 题目详情（含标签、判题配置） |
| GET | `/api/v1/judge/problem/options` | - | 题目选项列表 |

### 7.4 测试用例

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/testcase/page` | `current, size, problem_id` | 测试用例分页 |
| POST | `/api/v1/judge/testcase/create` | `{problem_id, type, input, output, score}` | 创建测试用例 |
| POST | `/api/v1/judge/testcase/modify` | `{id, ...}` | 编辑测试用例 |
| POST | `/api/v1/judge/testcase/remove` | `{ids:[]}` | 删除测试用例 |
| GET | `/api/v1/judge/testcase/detail` | `id` | 测试用例详情 |
| POST | `/api/v1/judge/testcase/upload` | `multipart(file)` | 上传测试文件 |

### 7.5 判题配置

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/judge/page` | `current, size` | 判题配置分页 |
| POST | `/api/v1/judge/judge/create` | `{problem_id, judge_type, ...}` | 创建判题配置 |
| POST | `/api/v1/judge/judge/modify` | `{id, ...}` | 编辑判题配置 |
| POST | `/api/v1/judge/judge/remove` | `{ids:[]}` | 删除判题配置 |
| GET | `/api/v1/judge/judge/detail` | `id` | 判题配置详情 |

### 7.6 提交

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| POST | `/api/v1/judge/submission/create` | `{problem_id, code, language, contest_id?}` | 提交代码（自动入队） |
| GET | `/api/v1/judge/submission/page` | `current, size, problem_id, user_id, status, contest_id` | 提交列表 |
| GET | `/api/v1/judge/submission/detail` | `id` | 提交详情（含每个测试点结果） |

### 7.7 竞赛

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/contest/page` | `current, size, status, type` | 竞赛列表（status: upcoming/running/ended） |
| POST | `/api/v1/judge/contest/create` | `{title, type, start_time, end_time, problem_ids, ...}` | 创建竞赛 |
| POST | `/api/v1/judge/contest/modify` | `{id, ...}` | 编辑竞赛 |
| POST | `/api/v1/judge/contest/remove` | `{ids:[]}` | 删除竞赛 |
| GET | `/api/v1/judge/contest/detail` | `id` | 竞赛详情 |
| POST | `/api/v1/judge/contest/register` | `{id, password?}` | 报名参赛 |
| GET | `/api/v1/judge/contest/rank` | `id` | 排行榜 |
| POST | `/api/v1/judge/contest/submit` | `{contest_id, problem_id, code, language}` | 比赛内提交 |

### 7.8 题单

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/problemset/page` | `current, size, keyword` | 题单列表 |
| POST | `/api/v1/judge/problemset/create` | `{title, description, problem_ids, visibility}` | 创建题单 |
| POST | `/api/v1/judge/problemset/modify` | `{id, ...}` | 编辑题单 |
| POST | `/api/v1/judge/problemset/remove` | `{ids:[]}` | 删除题单 |
| GET | `/api/v1/judge/problemset/detail` | `id` | 题单详情（含题目列表） |

---

## 八、配置新增

在 `config.yaml` 中新增 `judge` 配置段：

```yaml
# 判题系统配置
judge:
  sandbox:
    backends:
      - name: go-judge
        endpoint: localhost:5051
        timeout: 30
    # 可扩展其他后端：
    # - name: docker
    #   endpoint: unix:///var/run/docker.sock
    #   timeout: 60
  concurrent: 4                     # 并发 Worker 数
  health_check_interval: 30         # 健康检查间隔(秒)
  default_max_cpu: 10000000000      # 默认 CPU 时间限制(ns)
  default_max_memory: 268435456     # 默认内存限制(byte)
  spj_time_limit: 30000000000       # SPJ 自身时间限制(ns)
  compile_time_limit: 30000000000   # 编译时间限制(ns)
  queue_key: "judge:queue"          # Redis List key
```

---

## 九、实施顺序

| 步骤 | 内容 | 文件数 | 依赖 |
|---|---|---|---|
| 1 | `plugin.go` + `imports.go` — 插件骨架 | 2 | 无 |
| 2 | `sandbox/` — 抽象接口 + gojudge 适配器 + 健康检查 | 4 | 无 |
| 3 | `tag/` — 标签模块，打通完整 CRUD 链路 | 5 | 步骤1 |
| 4 | `problem/` — 题目模块，含标签关联（批量查防 N+1） | 5 | 步骤3 |
| 5 | `testcase/` — 测试用例模块 | 5 | 步骤4 |
| 6 | `judge/` — 判题配置 + 判题引擎 + SPJ + Interactive | 7 | 步骤2,5 |
| 7 | `submission/` — 提交 + Redis List 判题队列 | 6 | 步骤6 |
| 8 | `contest/` — 竞赛 + 排行榜（内存聚合防 N+1） | 7 | 步骤7 |
| 9 | `problemset/` — 题单模块 | 5 | 步骤4 |
| 10 | 注册到 `app/main.go` + `config.yaml` | 2 | 全部 |
| 11 | 编译验证 | - | 全部 |

**总计约 48 个文件**（含 API 路由、模型、服务、迁移）。

---

## 十、权限设计

参照 `plugin-sys` 的 `registry.Perm()` 模式，路由权限码统一前缀 `judge:`：

| 权限码 | 说明 |
|---|---|
| `judge:tag:page` | 标签分页 |
| `judge:tag:create` | 创建标签 |
| `judge:tag:modify` | 编辑标签 |
| `judge:tag:remove` | 删除标签 |
| `judge:problem:page` | 题目分页 |
| `judge:problem:create` | 创建题目 |
| `judge:problem:modify` | 编辑题目 |
| `judge:problem:remove` | 删除题目 |
| `judge:testcase:page` | 测试用例分页 |
| `judge:testcase:create` | 创建测试用例 |
| `judge:testcase:modify` | 编辑测试用例 |
| `judge:testcase:remove` | 删除测试用例 |
| `judge:judge:page` | 判题配置分页 |
| `judge:judge:create` | 创建判题配置 |
| `judge:judge:modify` | 编辑判题配置 |
| `judge:judge:remove` | 删除判题配置 |
| `judge:submission:page` | 提交列表 |
| `judge:submission:detail` | 提交详情 |
| `judge:contest:page` | 竞赛列表 |
| `judge:contest:create` | 创建竞赛 |
| `judge:contest:modify` | 编辑竞赛 |
| `judge:contest:remove` | 删除竞赛 |
| `judge:contest:rank` | 排行榜 |
| `judge:problemset:page` | 题单列表 |
| `judge:problemset:create` | 创建题单 |
| `judge:problemset:modify` | 编辑题单 |
| `judge:problemset:remove` | 删除题单 |
| `judge:sandbox:health` | 沙箱健康检查 |

提交和参赛等操作由登录中间件控制，不额外配权限码。

---

## 十一、go-judge gRPC 集成

### 11.1 通信方式变更

与 go-judge 的通信由 HTTP 改为 **gRPC**，使用 go-judge 原生的 protobuf 服务定义。

### 11.2 go-judge 的 gRPC 服务定义

go-judge（criyle/go-judge）通过 protobuf 定义了两组核心 gRPC 服务：

```protobuf
// ── 基础执行服务 ──
service Executor {
  // 单次执行
  rpc Exec(ExecRequest) returns (ExecResponse);
  // 批量执行
  rpc ExecBatch(ExecBatchRequest) returns (ExecBatchResponse);
}

// ── 文件服务 ──
service FileStore {
  // 上传文件到判题机
  rpc UploadFile(stream FileContent) returns (FileID);
  // 下载文件
  rpc DownloadFile(FileID) returns (stream FileContent);
  // 删除文件
  rpc RemoveFile(FileID) returns (Empty);
}
```

由于 go-judge 官方并未作为 Go module 发布可导入的 proto 生成代码，我们在项目内自建一套精简的 proto 定义并生成 Go 代码。

### 11.3 项目内 proto 结构

```
sdk/
├── gojudge/
│   ├── proto/
│   │   ├── executor.proto      # Executor 服务
│   │   └── file_store.proto    # FileStore 服务（可选）
│   ├── executor.pb.go          # protoc 生成
│   ├── executor_grpc.pb.go     # protoc 生成
│   └── file_store.pb.go        # protoc 生成（可选）
```

> **简化方案**：考虑到 go-judge 的 proto 定义相对成熟稳定，也可直接在 sandbox/gojudge/ 中定义 gRPC 客户端代码，手动编解码 protobuf 消息，避免引入 protoc 工具链。等后续需要完整 server 端时再引入正式 proto 生成。
>
> **推荐方案**：使用 `google.golang.org/grpc` 和 `google.golang.org/protobuf` 作为依赖，在 `sandbox/gojudge/` 下定义与 go-judge 匹配的消息类型和 gRPC 客户端调用代码。

### 11.4 适配器实现变更

```go
package gojudge

import (
    "context"
    "time"

    "google.golang.org/grpc"
    "google.golang.org/grpc/credentials/insecure"
    pb "hei-gin/sandbox/gojudge/proto"  // 或 inline 定义
)

type Backend struct {
    conn   *grpc.ClientConn
    client pb.ExecutorClient
    endpoint string
    timeout  time.Duration
}

func NewBackend(endpoint string, timeout time.Duration) (*Backend, error) {
    ctx, cancel := context.WithTimeout(context.Background(), timeout)
    defer cancel()
    conn, err := grpc.DialContext(ctx, endpoint,
        grpc.WithTransportCredentials(insecure.NewCredentials()),
        grpc.WithBlock(),
    )
    if err != nil {
        return nil, fmt.Errorf("connect go-judge %s: %w", endpoint, err)
    }
    return &Backend{
        conn:     conn,
        client:   pb.NewExecutorClient(conn),
        endpoint: endpoint,
        timeout:  timeout,
    }, nil
}

func (b *Backend) Exec(req *sandbox.ExecRequest) (*sandbox.ExecResult, error) {
    ctx, cancel := context.WithTimeout(context.Background(), b.timeout)
    defer cancel()
    pbReq := toProtoRequest(req)
    pbResp, err := b.client.Exec(ctx, pbReq)
    if err != nil {
        return nil, fmt.Errorf("go-judge exec: %w", err)
    }
    return toSandboxResult(pbResp), nil
}

func (b *Backend) Health() *sandbox.HealthStatus {
    ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
    defer cancel()
    _, err := b.client.Exec(ctx, &pb.ExecRequest{
        // 发送一个空请求检查服务是否存活
        Cmd: []*pb.ExecRequest_Cmd{{
            Args: []string{"echo", "health"},
        }},
    })
    alive := err == nil
    status := &sandbox.HealthStatus{
        Alive:       alive,
        BackendName: b.Name(),
    }
    if !alive {
        status.Error = err.Error()
    }
    return status
}
```

### 11.5 配置更新

```yaml
judge:
  sandbox:
    backends:
      - name: go-judge
        # gRPC 地址（无需 http:// 前缀）
        endpoint: localhost:5051
        timeout: 30
  # ... 其余配置不变
```

### 11.6 go.mod 新增依赖

```
require (
    google.golang.org/grpc v1.67.0
    google.golang.org/protobuf v1.36.0
)
```

### 11.7 文件变更

| 变更 | 说明 |
|---|---|
| `sandbox/gojudge/client.go` | HTTP 客户端 → 移除 |
| `sandbox/gojudge/adapter.go` | 改为 gRPC 客户端实现 |
| `sandbox/gojudge/proto/` | 新增 proto 目录，存放消息类型定义 |

---

## 十二、文件清单汇总

| 路径 | 行数估算 | 说明 |
|---|---|---|
| `plugin-judge/plugin.go` | ~40 | 插件注册 |
| `plugin-judge/imports.go` | ~30 | blank-imports |
| `plugin-judge/sandbox/types.go` | ~60 | 接口 + 类型定义 |
| `plugin-judge/sandbox/health.go` | ~50 | 健康检查 |
| `plugin-judge/sandbox/gojudge/adapter.go` | ~80 | gRPC 适配器 |
| `plugin-judge/tag/model.go` | ~15 | |
| `plugin-judge/tag/migrate.go` | ~8 | |
| `plugin-judge/tag/params.go` | ~25 | |
| `plugin-judge/tag/service.go` | ~80 | |
| `plugin-judge/tag/api/v1/api.go` | ~120 | |
| `plugin-judge/problem/model.go` | ~45 | |
| `plugin-judge/problem/migrate.go` | ~8 | |
| `plugin-judge/problem/params.go` | ~40 | |
| `plugin-judge/problem/service.go` | ~150 | 含标签关联批量查 |
| `plugin-judge/problem/api/v1/api.go` | ~150 | |
| `plugin-judge/testcase/model.go` | ~30 | |
| `plugin-judge/testcase/migrate.go` | ~8 | |
| `plugin-judge/testcase/params.go` | ~25 | |
| `plugin-judge/testcase/service.go` | ~120 | 含文件处理 |
| `plugin-judge/testcase/api/v1/api.go` | ~140 | |
| `plugin-judge/judge/model.go` | ~40 | |
| `plugin-judge/judge/migrate.go` | ~8 | |
| `plugin-judge/judge/params.go` | ~30 | |
| `plugin-judge/judge/engine.go` | ~120 | 判题核心调度 |
| `plugin-judge/judge/spj.go` | ~80 | Special Judge |
| `plugin-judge/judge/interactive.go` | ~80 | Interactive |
| `plugin-judge/judge/api/v1/api.go` | ~120 | |
| `plugin-judge/submission/model.go` | ~30 | |
| `plugin-judge/submission/migrate.go` | ~8 | |
| `plugin-judge/submission/params.go` | ~20 | |
| `plugin-judge/submission/service.go` | ~100 | |
| `plugin-judge/submission/queue.go` | ~100 | Redis List 队列 |
| `plugin-judge/submission/api/v1/api.go` | ~100 | |
| `plugin-judge/contest/model.go` | ~45 | |
| `plugin-judge/contest/migrate.go` | ~10 | |
| `plugin-judge/contest/params.go` | ~30 | |
| `plugin-judge/contest/service.go` | ~120 | CRUD + 报名 |
| `plugin-judge/contest/rank.go` | ~150 | 排行榜算法 |
| `plugin-judge/contest/api/v1/api.go` | ~180 | |
| `plugin-judge/problemset/model.go` | ~20 | |
| `plugin-judge/problemset/migrate.go` | ~8 | |
| `plugin-judge/problemset/params.go` | ~20 | |
| `plugin-judge/problemset/service.go` | ~80 | |
| `plugin-judge/problemset/api/v1/api.go` | ~120 | |
| `app/main.go` | +1行 | import 注册 |
| `config.yaml` | +~18行 | judge 配置段 |

---

## 十三、沙箱健康检查持久化与并发安全

### 13.1 沙箱实例管理（DB 持久化）

新增 `JudgeSandbox` 模型，将沙箱实例及其健康状态持久化到数据库：

```go
type JudgeSandbox struct {
    ID              string     `gorm:"primaryKey;size:32"`
    Name            string     `gorm:"size:32;not null"`                 // 后端名称: go-judge
    Endpoint        string     `gorm:"size:255;not null;uniqueIndex"`    // gRPC 地址: localhost:5051
    Status          string     `gorm:"size:16;default:active"`           // active / offline / removed
    Version         *string    `gorm:"size:32"`
    LastHealthCheck *time.Time
    LastHealthPass  *time.Time
    ConsecutiveFail int        `gorm:"default:0"`                        // 连续失败次数
    ErrorMessage    *string    `gorm:"size:500"`
    CreatedAt       time.Time
    UpdatedAt       *time.Time
}

func (JudgeSandbox) TableName() string { return "judge_sandbox" }
```

**健康检查流程：**

```
定时器触发（每 health_check_interval 秒）
    │
    ├→ 查询 DB 中 Status = active 或 offline 的沙箱实例
    │
    ├→ 对每个实例执行 gRPC Health() 探活
    │      │
    │      ├→ 成功 → 更新 DB:
    │      │   status = active
    │      │   last_health_check = now
    │      │   last_health_pass = now
    │      │   consecutive_fail = 0
    │      │   error_message = null
    │      │
    │      └→ 失败 → 更新 DB:
    │          consecutive_fail += 1
    │          └→ consecutive_fail >= max_retry (默认3次)
    │               → status = offline
    │               → 从内存池中移除
    │
    └→ 同时检测 offline 实例是否可恢复
         → 对 Status = offline 且距上次检测超过 recovery_interval 的实例重新探活
         → 成功则 status = active，重新加入内存池
```

### 13.2 配置新增

```yaml
judge:
  sandbox:
    backends:
      - name: go-judge
        endpoint: localhost:5051
        timeout: 30
    health_check:
      interval: 30                    # 健康检查周期（秒）
      max_retry: 3                    # 连续失败次数后下线
      recovery_interval: 120          # 下线后每隔多久尝试恢复（秒）
    # ...
```

### 13.3 内存池（并发安全）

为减少每次判题都查 DB，维护一个 **并发安全的内存池** 存储当前活跃的沙箱后端：

```go
package sandbox

import (
    "math/rand"
    "sync"
)

// Pool 沙箱后端连接池（并发安全）
type Pool struct {
    mu       sync.RWMutex
    backends []*poolEntry       // 当前活跃后端
    roundRobin uint64           // 轮询计数器
}

type poolEntry struct {
    backend SandboxBackend
    weight  int                 // 预留：后续支持权重
}

var DefaultPool = &Pool{}

// Get 获取一个可用的后端（轮询调度）
func (p *Pool) Get() SandboxBackend {
    p.mu.RLock()
    defer p.mu.RUnlock()
    n := len(p.backends)
    if n == 0 {
        return nil
    }
    if n == 1 {
        return p.backends[0].backend
    }
    idx := atomic.AddUint64(&p.roundRobin, 1) % uint64(n)
    return p.backends[idx].backend
}

// GetAll 返回所有可用后端（健康检查使用）
func (p *Pool) GetAll() []SandboxBackend {
    p.mu.RLock()
    defer p.mu.RUnlock()
    result := make([]SandboxBackend, len(p.backends))
    for i, e := range p.backends {
        result[i] = e.backend
    }
    return result
}

// ReplaceAll 原子替换整个后端列表（健康检查完成后调用）
func (p *Pool) ReplaceAll(backends []SandboxBackend) {
    p.mu.Lock()
    defer p.mu.Unlock()
    entries := make([]*poolEntry, len(backends))
    for i, b := range backends {
        entries[i] = &poolEntry{backend: b, weight: 1}
    }
    p.backends = entries
    // 重置轮询计数器，避免新后端上来被跳过
    atomic.StoreUint64(&p.roundRobin, 0)
}

// Count 当前活跃后端数
func (p *Pool) Count() int {
    p.mu.RLock()
    defer p.mu.RUnlock()
    return len(p.backends)
}
```

### 13.4 并发场景梳理

| 场景 | 问题 | 方案 |
|---|---|---|
| 多 Worker 同时 `Get()` | 并发读写 `backends` 切片 | `sync.RWMutex` — `Get()` 用 RLock，多 Worker 可并行 |
| 健康检查 `ReplaceAll()` | 与 Worker `Get()` 冲突 | 写锁阻塞读锁，`ReplaceAll` 期间 Worker 等待（纳秒级） |
| Worker 判题中后端突然离线 | 已拿到的 backend 句柄仍在用 | gRPC 调用本身有 timeout；失败后 Worker 重试 `Get()` 换一个 |
| 轮询计数器溢出 | `uint64` 溢出后归零 | 正常行为，溢出不影响公平性 |
| 首次启动无可用后端 | 所有提交都失败 | `Get()` 返回 nil → submission 状态置为 SE，错误提示"无可用判题节点" |
| DB 中大量 offline 实例 | 每次健康检查都查全部 | 只查 `active` + `offline`（排除 `removed`），且 offline 按 `recovery_interval` 降频检测 |

### 13.5 启动与关闭流程

```go
func (p *JudgePlugin) Init() error {
    // 1. 从 DB 加载所有 Status = active 的沙箱实例
    // 2. 为每个实例创建 gRPC 连接
    // 3. 填充到 Pool
    return nil
}

func (p *JudgePlugin) Start() error {
    // 1. 启动 N 个判题 Worker（goroutine）
    // 2. 启动健康检查循环（goroutine）
    return nil
}

func (p *JudgePlugin) Stop() error {
    // 1. 关闭 stopCh，通知所有 Worker 和健康检查退出
    // 2. 等待所有 goroutine 结束
    // 3. 关闭所有 gRPC 连接
    // 4. 记录下线日志
    return nil
}
```

### 13.6 新增/变更文件

| 文件 | 变更 |
|---|---|
| `plugin-judge/sandbox/types.go` | +Pool 定义 |
| `plugin-judge/sandbox/pool.go` | **新增**：并发安全连接池 |
| `plugin-judge/sandbox/health.go` | 重写：DB 持久化 + 自动下线/恢复 |
| `plugin-judge/sandbox/model.go` | **新增**：JudgeSandbox GORM 模型 |
| `plugin-judge/sandbox/migrate.go` | **新增**：注册模型 |

### 13.7 管理 API

扩展沙箱管理 API：

| 方法 | 路径 | 参数 | 说明 |
|---|---|---|---|
| GET | `/api/v1/judge/sandbox/health` | - | 当前所有沙箱实例状态（从 DB 读取） |
| POST | `/api/v1/judge/sandbox/create` | `{name, endpoint, timeout}` | 注册新沙箱实例 |
| POST | `/api/v1/judge/sandbox/modify` | `{id, name, endpoint, timeout}` | 编辑沙箱配置 |
| POST | `/api/v1/judge/sandbox/remove` | `{ids:[]}` | 软删除沙箱实例（status=removed） |

