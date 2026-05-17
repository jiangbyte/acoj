# 主题与布局

框架提供灵活的主题定制和布局系统。

## 主题系统

### 主题模式

支持 `light`（浅色）、`dark`（暗黑）、`realDark`（真实暗黑）三种主题模式：

```typescript
const appStore = useAppStore()
appStore.setTheme('realDark')  // 切换暗黑主题（Ant Design 暗黑算法）
appStore.setTheme('light')     // 切换浅色主题
```

### 主色定制

基于 `@ant-design/colors` 色板生成算法，动态生成 CSS 变量并注入 Ant Design 主题配置：

```typescript
appStore.setColorPrimary('#1677ff')  // Ant Design 默认蓝
appStore.setColorPrimary('#f5222d')  // 红色系
```

### 辅助设置

| 功能 | 说明 |
|------|------|
| 灰色模式 | 全站灰度显示（用于特殊纪念日） |
| 色弱模式 | 适配色弱用户 |
| 圆角控制 | 全局卡片和组件的圆角开关 |

## 布局系统

### 布局模式

| 模式 | 说明 | 状态 |
|------|------|------|
| vertical | 垂直布局（左侧菜单 + 顶部栏 + 内容区） | 当前唯一模式 |

布局配置存储在 `src/store/app.ts` 的 Pinia store 中，支持持久化。

### 布局组件

```
base-layout/
├── index.vue              # 布局主框架
├── vertical.vue           # 垂直布局实现
├── sider/                 # 侧边栏
│   ├── index.vue
│   ├── menuHelper.ts
│   └── useMenu.ts
├── header/                # 顶部栏
│   └── index.vue
├── tab/                   # 标签页
│   └── index.vue
├── breadcrumb/            # 面包屑
│   └── index.vue
└── components/            # 布局组件
    ├── Logo.vue           # Logo
    ├── UserAvatar.vue     # 用户头像
    ├── UserDrawer.vue     # 用户信息抽屉
    ├── ThemeDrawer.vue    # 主题设置抽屉
    ├── MobileDrawer.vue   # 移动端菜单抽屉
    └── FooterBar.vue      # 页脚
```

### 标签页系统

内置多标签页导航，支持：

- 标签页的添加和关闭（当前页 / 其他 / 全部）
- 页面缓存（通过 `keep-alive` 和路由 `meta.cache` 控制）
- 标签页持久化（刷新后恢复）
- 固定标签页（路由 `meta.affix` 属性）

### 响应式适配

- 桌面端 700px 以上显示完整布局
- 移动端 700px 以下自动切换为抽屉式导航
- 移动端通过 `MobileDrawer` 组件展示菜单
