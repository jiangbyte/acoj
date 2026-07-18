<script setup lang="ts">
import { ojJudgeApi } from '@/api'
import { formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  runtimeVersion: null as any,
})

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.runtimeVersion = null
  try {
    const response = await ojJudgeApi.runtimeVersion.detail({ id })
    state.runtimeVersion = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="560">
    <NDrawerContent title="运行时版本详情" closable>
      <NSpin :show="state.loading">
        <NDescriptions v-if="state.runtimeVersion" :column="1" bordered size="small">
          <NDescriptionsItem label="id">{{ state.runtimeVersion.id }}</NDescriptionsItem>
          <NDescriptionsItem label="judge_node_id">{{ state.runtimeVersion.judge_node_id }}</NDescriptionsItem>
          <NDescriptionsItem label="language_id">{{ state.runtimeVersion.language_id }}</NDescriptionsItem>
          <NDescriptionsItem label="runtime_name">{{ state.runtimeVersion.runtime_name }}</NDescriptionsItem>
          <NDescriptionsItem label="runtime_version">{{ state.runtimeVersion.runtime_version || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="priority">{{ state.runtimeVersion.priority }}</NDescriptionsItem>
          <NDescriptionsItem label="created_at">{{ formatDateTime(state.runtimeVersion.created_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="updated_at">{{ formatDateTime(state.runtimeVersion.updated_at) }}</NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
