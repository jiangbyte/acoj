<!--
  Author: Charlie

  OJ 执行机详情弹窗。
-->
<script setup lang="ts">
import { ojJudgeNodeApi } from '@/api'
import { createTagColor, dictTypeColor, dictTypeData, displayValue, formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  detail: {} as any,
})

async function openModal(id: string) {
  state.detail = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojJudgeNodeApi.detail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

function formatJsonValue(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return Array.isArray(value) ? '[]' : '{}'
  }
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

defineExpose({
  openModal,
})
</script>

<template>
  <HeiDetailContainer
    v-model:show="state.showModal"
    title="执行机详情"
    :width="720"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <NDescriptions
        label-placement="left"
        bordered
        :column="1"
      >
        <NDescriptionsItem label="ID">
          {{ displayValue(state.detail.id) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="编码">
          {{ displayValue(state.detail.code) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="名称">
          {{ displayValue(state.detail.name) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="地址">
          {{ displayValue(state.detail.base_url) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="开启验签">
          {{ displayValue(state.detail.signing_enabled) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="管理状态">
          <NTag
            :color="createTagColor(dictTypeColor('OJ_JUDGE_ADMIN_STATUS', state.detail.admin_status))"
            :bordered="false"
          >
            {{
              dictTypeData('OJ_JUDGE_ADMIN_STATUS', state.detail.admin_status) ||
                displayValue(state.detail.admin_status)
            }}
          </NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="运行状态">
          {{ displayValue(state.detail.runtime_status) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="熔断状态">
          {{ displayValue(state.detail.circuit_state) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="在途数">
          {{ displayValue(state.detail.inflight_count) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="权重">
          {{ displayValue(state.detail.weight) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="优先级">
          {{ displayValue(state.detail.priority) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最大并发">
          {{ displayValue(state.detail.max_concurrency) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="支持语言">
          <NCode
            :code="formatJsonValue(state.detail.supported_languages ?? [])"
            language="json"
            word-wrap
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="最近心跳">
          {{ formatDateTime(state.detail.last_heartbeat_at) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最近错误">
          {{ displayValue(state.detail.last_error_message) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="扩展">
          <NCode
            :code="formatJsonValue(state.detail.extra)"
            language="json"
            word-wrap
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          {{ formatDateTime(state.detail.created_at) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="更新时间">
          {{ formatDateTime(state.detail.updated_at) }}
        </NDescriptionsItem>
      </NDescriptions>
    </NSpin>
  </HeiDetailContainer>
</template>
