<!--
  Author: Charlie

  操作审计详情。
-->
<script setup lang="ts">
import { auditApi } from '@/api'
import { accountTypeLabel } from '@/constants/account'
import {
  auditActionName,
  auditActionTypeLabel,
  auditDurationText,
  auditModuleLabel,
  auditOperatorName,
} from '@/utils/audit'
import { displayValue, formatDateTime } from '@/utils'
import { wireBool } from '@/utils/wire'
import { computed, reactive } from 'vue'

const props = withDefaults(
  defineProps<{
    /** 个人中心：走 my-detail，无需审计管理权限 */
    selfOnly?: boolean
  }>(),
  { selfOnly: false },
)

const state = reactive({
  showModal: false,
  loading: false,
  record: {} as any,
})

const successText = computed(() => {
  if (state.record?.success === undefined || state.record?.success === null) {
    return '-'
  }
  return wireBool(state.record.success) ? '成功' : '失败'
})

function formatJson(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

async function openModal(id: string) {
  state.record = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = props.selfOnly
      ? await auditApi.myDetail(id)
      : await auditApi.detail({ id })
    state.record = response.data ?? {}
  } finally {
    state.loading = false
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <HeiDetailContainer
    v-model:show="state.showModal"
    title="审计详情"
    :width="760"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <NDescriptions
        label-placement="left"
        bordered
        :column="1"
        label-style="min-width: 120px"
      >
        <NDescriptionsItem label="日志编号">
          {{ displayValue(state.record.id) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作模块">
          {{ auditModuleLabel(state.record) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作名">
          {{ auditActionName(state.record) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作类型">
          {{ auditActionTypeLabel(state.record.action_type) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作人">
          {{ auditOperatorName(state.record) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作内容">
          <pre class="audit-pre">{{ displayValue(state.record.summary) }}</pre>
        </NDescriptionsItem>
        <NDescriptionsItem label="操作结果">
          {{ successText }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作时间">
          {{ formatDateTime(state.record.created_at) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="执行时长">
          {{ auditDurationText(state.record.duration_ms) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="业务编号">
          {{ displayValue(state.record.resource_id) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="账号类型">
          {{ state.record.account_type ? accountTypeLabel(state.record.account_type) : '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="请求 ID">
          {{ displayValue(state.record.request_id) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="IP">
          {{ displayValue(state.record.ip) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="User-Agent">
          <pre class="audit-pre">{{ displayValue(state.record.user_agent) }}</pre>
        </NDescriptionsItem>
        <NDescriptionsItem label="错误信息">
          <pre class="audit-pre">{{ displayValue(state.record.error_message) }}</pre>
        </NDescriptionsItem>
        <NDescriptionsItem label="变更前">
          <pre class="audit-pre">{{ formatJson(state.record.before_data) }}</pre>
        </NDescriptionsItem>
        <NDescriptionsItem label="变更后">
          <pre class="audit-pre">{{ formatJson(state.record.after_data) }}</pre>
        </NDescriptionsItem>
      </NDescriptions>
    </NSpin>
  </HeiDetailContainer>
</template>

<style scoped>
.audit-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.5;
}
</style>
