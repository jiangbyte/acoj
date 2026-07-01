<template>
  <n-space vertical size="large">
    <n-page-header :title="t('resource.demo.list')" :subtitle="t('resource.demo.list_subtitle')" />

    <n-card :bordered="false">
      <n-data-table :columns="columns" :data="data" :pagination="{ pageSize: 5 }" />
    </n-card>
  </n-space>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { NButton, NTag } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

type Level = 'easy' | 'medium' | 'hard'

const columns = computed(() => [
  {
    title: t('resource.demo.problem'),
    key: 'title',
  },
  {
    title: t('resource.demo.difficulty'),
    key: 'level',
    render(row: { level: Level }) {
      const type = row.level === 'easy' ? 'success' : row.level === 'medium' ? 'warning' : 'error'
      return h(NTag, { type, bordered: false }, { default: () => t(`resource.demo.${row.level}`) })
    },
  },
  {
    title: t('resource.demo.submissions'),
    key: 'submissions',
  },
  {
    title: t('resource.demo.actions'),
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
        { default: () => t('common.view') },
      )
    },
  },
])

const data = computed(() => [
  { id: 1001, title: t('resource.demo.two_sum'), level: 'easy' as Level, submissions: 248 },
  { id: 1002, title: t('resource.demo.merge_intervals'), level: 'medium' as Level, submissions: 136 },
  { id: 1003, title: t('resource.demo.shortest_path'), level: 'hard' as Level, submissions: 78 },
  { id: 1004, title: t('resource.demo.valid_parentheses'), level: 'easy' as Level, submissions: 302 },
  {
    id: 1005,
    title: t('resource.demo.dynamic_programming_intro'),
    level: 'medium' as Level,
    submissions: 119,
  },
])
</script>
