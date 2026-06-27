<template>
  <n-space vertical size="large">
    <n-page-header :title="t('demo.listTitle')" :subtitle="t('demo.listSubtitle')" />

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
    title: t('demo.problem'),
    key: 'title',
  },
  {
    title: t('demo.difficulty'),
    key: 'level',
    render(row: { level: Level }) {
      const type = row.level === 'easy' ? 'success' : row.level === 'medium' ? 'warning' : 'error'
      return h(NTag, { type, bordered: false }, { default: () => t(`demo.${row.level}`) })
    },
  },
  {
    title: t('demo.submissions'),
    key: 'submissions',
  },
  {
    title: t('demo.actions'),
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
  { id: 1001, title: t('demo.twoSum'), level: 'easy' as Level, submissions: 248 },
  { id: 1002, title: t('demo.mergeIntervals'), level: 'medium' as Level, submissions: 136 },
  { id: 1003, title: t('demo.shortestPath'), level: 'hard' as Level, submissions: 78 },
  { id: 1004, title: t('demo.validParentheses'), level: 'easy' as Level, submissions: 302 },
  {
    id: 1005,
    title: t('demo.dynamicProgrammingIntro'),
    level: 'medium' as Level,
    submissions: 119,
  },
])
</script>
