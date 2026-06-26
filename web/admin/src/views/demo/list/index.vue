<template>
  <n-space vertical size="large">
    <n-page-header title="列表示例" subtitle="简单表格和隐藏详情页跳转" />

    <n-card :bordered="false">
      <n-data-table :columns="columns" :data="data" :pagination="{ pageSize: 5 }" />
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { h } from 'vue'
import { NButton, NTag } from 'naive-ui'
import { useRouter } from 'vue-router'

const router = useRouter()

const columns = [
  {
    title: '题目',
    key: 'title',
  },
  {
    title: '难度',
    key: 'level',
    render(row: { level: string }) {
      const type = row.level === '简单' ? 'success' : row.level === '中等' ? 'warning' : 'error'
      return h(NTag, { type, bordered: false }, { default: () => row.level })
    },
  },
  {
    title: '提交',
    key: 'submissions',
  },
  {
    title: '操作',
    key: 'actions',
    render(row: { id: number }) {
      return h(
        NButton,
        {
          size: 'small',
          text: true,
          type: 'primary',
          onClick: () => router.push({ path: '/demo/detail', query: { id: row.id } }),
        },
        { default: () => '查看' },
      )
    },
  },
]

const data = [
  { id: 1001, title: '两数之和', level: '简单', submissions: 248 },
  { id: 1002, title: '区间合并', level: '中等', submissions: 136 },
  { id: 1003, title: '最短路径', level: '困难', submissions: 78 },
  { id: 1004, title: '括号匹配', level: '简单', submissions: 302 },
  { id: 1005, title: '动态规划入门', level: '中等', submissions: 119 },
]
</script>
