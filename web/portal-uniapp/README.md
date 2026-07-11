# Hei Uniapp

<img width="120" src="https://jiangbyte.github.io/hei-docs/logo.svg">

**Hei Uniapp** 是 HEI 快速开发框架的跨平台移动端解决方案，基于 UniApp + Vue 3 构建，一套代码可编译到 iOS、Android、H5、以及各类小程序平台。

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![UniApp](https://img.shields.io/badge/UniApp-3.x-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.3+-cyan.svg)

## 简介

Hei Uniapp 提供开箱即用的跨平台移动端开发解决方案，与 Hei Boot 后端框架配套使用，支持快速搭建移动端应用。项目采用 uni-app 生态，支持一套代码多端运行，覆盖 iOS、Android、H5、微信小程序、支付宝小程序等主流平台。

**在线文档**: [https://jiangbyte.github.io/hei-docs/hei-uniapp/](https://jiangbyte.github.io/hei-docs/hei-uniapp/)

## 技术栈

| 类型     | 技术                                       |
| -------- | ------------------------------------------ |
| 核心框架 | UniApp 3.x、Vue 3.3+                       |
| 编译平台 | H5、iOS、Android、微信小程序、支付宝小程序 |
| 状态管理 | Pinia                                      |
| UI 组件  | uni-ui、ColorUI                            |
| 网络请求 | uni-request、Axios                         |
| 工具库   | Lodash、Day.js                             |

## 项目结构

```
hei-uniapp/
├── src/
│   ├── api/            # API 接口
│   ├── components/     # 公共组件
│   ├── pages/          # 页面文件
│   ├── static/         # 静态资源
│   ├── stores/         # 状态管理
│   ├── styles/         # 全局样式
│   ├── utils/          # 工具函数
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── package.json        # 项目配置
├── manifest.json       # App 配置
├── pages.json          # 页面路由配置
└── uni.scss            # 全局样式变量
```

## 相关项目

- **[Hei Boot](../hei-boot)** - Java 后端框架
- **[Hei Cloud](../hei-cloud)** - Java 微服务架构
- **[Hei Admin Vue](../hei-admin-vue)** - Vue3 前端管理后台

## 参与贡献

我们非常欢迎社区贡献！

1. Fork 本仓库
2. 新建 `Feat_xxx` 分支
3. 提交代码
4. 创建 Pull Request

感谢所有为 HEI 项目做出贡献的开发者！

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议

## 联系方式

- [Gitee](https://gitee.com/jiangbyte/hei-uniapp)
- [GitHub](https://github.com/jiangbyte/hei-uniapp)
- [掘金](https://juejin.cn/user/1968540037686224)

---

如果这个项目对你有帮助，请给一个 Star 支持！
