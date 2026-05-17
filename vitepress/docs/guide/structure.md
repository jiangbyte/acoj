# 项目结构

```
hei-admin-vue/
├── build/                    # 构建配置
│   └── proxy.ts             # Vite 代理配置（按环境变量解析）
├── public/                   # 静态资源（不会被编译）
├── src/
│   ├── api/                  # API 接口层（Alova 请求）
│   │   ├── auth.ts           # 认证相关（登录/登出/用户信息/菜单/权限/SM2 公钥）
│   │   ├── dashboard.ts      # 仪表盘数据
│   │   ├── user.ts           # 用户管理
│   │   ├── role.ts           # 角色管理
│   │   ├── resource.ts       # 资源管理（菜单/按钮）
│   │   ├── org.ts            # 组织架构
│   │   ├── group.ts          # 用户组
│   │   ├── position.ts       # 岗位管理
│   │   ├── dict.ts           # 字典管理
│   │   ├── config.ts         # 系统配置
│   │   ├── notice.ts         # 公告管理
│   │   ├── banner.ts         # 横幅管理
│   │   ├── file.ts           # 文件管理
│   │   ├── log.ts            # 日志管理
│   │   ├── monitor.ts        # 在线监控
│   │   ├── client-user.ts    # 客户端用户管理
│   │   ├── permission.ts     # 权限管理
│   │   └── home.ts           # 首页数据
│   ├── components/           # 公共组件
│   │   ├── form/             # 表单组件（搜索面板、抽屉表单、字典选择）
│   │   ├── layout/           # 布局组件（分割面板、树形面板）
│   │   ├── result/           # 结果组件（403 页面）
│   │   └── table/            # 表格组件（CRUD 表格、树形表格）
│   ├── composables/          # 组合式函数
│   ├── hooks/                # 自定义 Hooks
│   │   ├── useCrud.ts        # CRUD 通用逻辑（分页、搜索、批量操作）
│   │   └── useMobile.ts      # 移动端适配
│   ├── layouts/              # 布局组件
│   │   ├── base-layout/      # 基础布局（侧边栏、顶部栏、标签页、主题抽屉）
│   │   └── blank-layout/     # 空白布局（登录/注册页）
│   ├── router/               # 路由配置
│   │   ├── index.ts          # 路由实例（基础路由 + 静态路由）
│   │   ├── routes.ts         # 静态路由定义（登录/注册/403/404）
│   │   ├── dynamic.ts        # 动态路由生成（从后端菜单配置生成 Vue Router）
│   │   └── guard.ts          # 路由守卫（认证检查、动态路由初始化）
│   ├── store/                # 状态管理（Pinia）
│   │   ├── index.ts          # Store 导出入口
│   │   ├── app.ts            # 应用状态（布局、主题、加载状态）
│   │   ├── auth.ts           # 认证状态（Token、用户信息、权限、SM2 公钥）
│   │   ├── dict.ts           # 字典缓存
│   │   ├── route.ts          # 路由状态（菜单、动态路由）
│   │   └── tab.ts            # 标签页状态
│   ├── styles/               # 全局样式（global.scss）
│   ├── types/                # TypeScript 类型定义
│   │   ├── global.d.ts       # 全局类型扩展
│   │   ├── service.d.ts      # 服务端响应类型
│   │   └── vite-env.d.ts     # Vite 环境类型
│   ├── utils/                # 工具函数
│   │   ├── http/             # Alova 实例封装
│   │   │   ├── alova.ts      # Alova 实例创建（Fetch/XHR 双适配器）
│   │   │   ├── config.ts     # HTTP 配置常量
│   │   │   ├── handle.ts     # 业务/网络错误处理
│   │   │   └── index.ts      # HTTP 工具导出
│   │   ├── confirm.ts        # 确认对话框工具
│   │   ├── dictTool.ts       # 字典工具（标签、颜色、选项列表）
│   │   ├── download.ts       # 文件下载工具
│   │   ├── iconUtil.ts       # 图标工具
│   │   ├── themeUtil.ts      # 主题工具（Ant Design 主题算法）
│   │   └── index.ts          # 工具函数导出
│   ├── views/                # 页面组件
│   │   ├── auth/             # 登录/注册/在线监控
│   │   ├── client/           # 客户端用户管理
│   │   ├── dashboard/        # 仪表盘
│   │   ├── error/            # 403/404 错误页
│   │   ├── home/             # 首页
│   │   └── sys/              # 系统管理（用户/角色/资源/组织/字典/配置/公告/横幅/文件/日志）
│   ├── App.vue               # 根组件
│   └── main.ts               # 入口文件
├── .env                      # 环境变量
├── index.html                # HTML 入口
├── package.json              # 项目配置
├── vite.config.ts            # Vite 构建配置
├── tsconfig.json             # TypeScript 配置
├── uno.config.ts             # UnoCSS 配置
└── eslint.config.js          # ESLint 配置
```

## 关键约定

### API 层

每个业务模块对应一个 `src/api/` 下的文件，按功能模块命名（`user.ts`、`role.ts` 等）。

### 页面组件

- `src/views/sys/` 下存放系统管理相关页面
- 每个模块包含 `index.vue`（列表页）、`components/form.vue`（表单弹窗）、`components/detail.vue`（详情弹窗）

### 组件层级

公共组件按类型分目录存放（`form/`、`table/`、`layout/`、`result/`），业务组件放在页面目录下的 `components/` 中。
