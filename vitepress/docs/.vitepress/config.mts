import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'zh-CN',
  title: 'Hei Admin Vue',
  description: 'Hei Admin Vue - 基于 Vue 3 + Vite + Ant Design Vue 的后台管理解决方案',
  base: '/hei-admin-vue/',

  head: [['link', { rel: 'icon', href: '/logo.svg' }]],

  themeConfig: {
    logo: '/logo.svg',
    
    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/' },
      { text: '功能', link: '/features/' },
      { text: '模块开发', link: '/modules/' },
      {
        text: '相关项目',
        items: [
          { text: 'Hei Gin (Go)', link: 'https://github.com/jiangbyte/hei-gin' },
          { text: 'Hei Boot (Java)', link: 'https://github.com/jiangbyte/hei-boot' },
          { text: 'Hei FastAPI (Python)', link: 'https://github.com/jiangbyte/hei-fastapi' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/jiangbyte/hei-admin-vue' },
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2026-present Charlie',
    },

    search: {
      provider: 'local',
    },

    outline: {
      level: [2, 3],
      label: '本页目录',
    },

    sidebar: {
      '/guide/': [
        { text: '介绍', link: '/overview' },
        { text: '快速开始', link: '/guide/quickstart' },
        { text: '项目结构', link: '/guide/structure' },
      ],
      '/features/': [
        { text: '功能概览', link: '/features/' },
        { text: '认证体系', link: '/features/auth' },
        { text: '权限管理', link: '/features/permission' },
        { text: '主题与布局', link: '/features/theme-layout' },
        { text: '组件体系', link: '/features/components' },
        { text: 'HTTP 请求封装', link: '/features/http' },
      ],
      '/modules/': [
        { text: '模块开发规范', link: '/modules/development' },
      ],
    },
  },

  markdown: {
    lineNumbers: true,
  },

  ignoreDeadLinks: true,
})
