<template>
  <n-space vertical size="large">
    <n-page-header title="示例总览" subtitle="静态路由、菜单和标签页示例" />

    <n-grid cols="1 s:2 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item v-for="item in stats" :key="item.label">
        <n-card :title="item.title" :bordered="false">
          <n-statistic :label="item.label" :value="item.value" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="最近操作" :bordered="false">
      <n-timeline>
        <n-timeline-item type="success" title="路由初始化" content="加载静态路由表" />
        <n-timeline-item type="info" title="菜单生成" content="根据 RowRoute 生成侧边菜单" />
        <n-timeline-item type="warning" title="缓存页面" content="is_cache 页面进入缓存列表" />
      </n-timeline>
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
const stats = [
  { title: '路由数量', label: '静态示例', value: 6 },
  { title: '菜单页面', label: '可见页面', value: 4 },
  { title: '隐藏页面', label: '详情页', value: 1 },
  { title: '缓存页面', label: 'is_cache', value: 1 },
]
</script>
