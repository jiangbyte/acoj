<!--
  Author: Charlie

  个人中心：本人登录日志列表。
-->
<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'
import { NTag } from 'naive-ui'
import { auditApi } from '@/api'
import { createTagColor, formatDateTime } from '@/utils'
import { wireBool, readPageMeta } from '@/utils/wire'
import { computed, h, onMounted, reactive } from 'vue'
import '../profile.css'

const state = reactive({
  loading: false,
  rows: [] as any[],
  total: 0,
  page: 1,
  pageSize: 10,
})

const columns = computed<DataTableColumns<any>>(() => [
  {
    title: '操作时间',
    key: 'created_at',
    width: 170,
    render: (row) => formatDateTime(row.created_at),
  },
  {
    title: '操作结果',
    key: 'success',
    width: 80,
    render: (row) =>
      h(
        NTag,
        {
          size: 'small',
          bordered: false,
          color: createTagColor(wireBool(row.success) ? '#52c41a' : '#ff4d4f'),
        },
        { default: () => (wireBool(row.success) ? '成功' : '失败') },
      ),
  },
  {
    title: '操作内容',
    key: 'summary',
    ellipsis: { tooltip: true },
    render: (row) => row.summary || '-',
  },
  {
    title: 'IP',
    key: 'ip',
    width: 130,
    render: (row) => row.ip || '-',
  },
  {
    title: 'User-Agent',
    key: 'user_agent',
    ellipsis: { tooltip: true },
    render: (row) => row.user_agent || '-',
  },
])

onMounted(() => {
  void fetchPage()
})

async function fetchPage() {
  state.loading = true
  try {
    const response = await auditApi.myPage({
      current: state.page,
      size: state.pageSize,
      action: 'login',
    } as any)
    const data = response.data ?? {}
    state.rows = Array.isArray(data.records) ? data.records : []
    const pageMeta = readPageMeta(data, { current: state.page, size: state.pageSize })
    state.total = pageMeta.total
    state.page = pageMeta.current
    state.pageSize = pageMeta.size
  } finally {
    state.loading = false
  }
}

function handlePageChange(page: number) {
  state.page = page
  void fetchPage()
}

function handlePageSizeChange(size: number) {
  state.pageSize = size
  state.page = 1
  void fetchPage()
}
</script>

<template>
  <div class="w-full min-w-0">
    <NSpace
      justify="end"
      class="profile-panel-toolbar"
    >
      <NButton
        text
        :loading="state.loading"
        @click="fetchPage"
      >
        刷新
      </NButton>
    </NSpace>

    <NSpin :show="state.loading">
      <NEmpty
        v-if="!state.loading && !state.rows.length"
        description="暂无登录记录"
      />
      <NDataTable
        v-else
        class="profile-log__table"
        size="small"
        :bordered="false"
        :single-line="false"
        :columns="columns"
        :data="state.rows"
        :pagination="false"
      />
    </NSpin>

    <NSpace
      v-if="state.total > 0"
      justify="end"
      class="mt-3"
    >
      <NPagination
        :page="state.page"
        :page-size="state.pageSize"
        :item-count="state.total"
        :page-sizes="[10, 20, 30]"
        show-size-picker
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </NSpace>
  </div>
</template>
