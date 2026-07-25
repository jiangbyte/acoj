<script setup lang="ts">
const features = [
  {
    icon: 'icon-park-outline:rocket',
    title: '快速启动',
    description: '基于 Nuxt 4 + Nuxt UI v4，开箱即用的项目模板。',
  },
  {
    icon: 'icon-park-outline:api-app',
    title: 'API 集成',
    description: '内置 Axios 实例，轻松对接后端 FastAPI 服务。',
  },
  {
    icon: 'icon-park-outline:dark-mode',
    title: '深色模式',
    description: '内置明暗主题切换，自动适配系统偏好。',
  },
  {
    icon: 'icon-park-outline:layout-one',
    title: '响应式布局',
    description: '移动端优先设计，完美适配各种屏幕尺寸。',
  },
  {
    icon: 'icon-park-outline:waterfalls-v',
    title: '状态管理',
    description: 'Pinia + 持久化插件，状态管理简单高效。',
  },
  {
    icon: 'icon-park-outline:check-one',
    title: '代码规范',
    description: 'ESLint + Prettier 保障代码质量和统一风格。',
  },
]
</script>

<template>
  <div>
    <UPageHero
      title="HEI"
      description="基于 Nuxt 4 的企业级前端门户框架，集成 Nuxt UI v4 组件库、Pinia 状态管理、TypeScript 类型支持。"
      :links="[
        { label: '快速开始', to: '/docs', size: 'lg' },
        {
          label: '查看源码',
          color: 'neutral',
          variant: 'outline',
          to: 'https://github.com',
          target: '_blank',
          size: 'lg',
        },
      ]"
    />

    <UPageSection
      id="features"
      title="核心特性"
      description="为现代 Web 应用开发提供完整的基础设施"
      :features="features"
    />
  </div>
</template>
