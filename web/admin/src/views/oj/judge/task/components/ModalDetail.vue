<script setup lang="ts">
import { ojJudgeApi } from '@/api'
import { formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  task: null as any,
})

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.task = null
  try {
    const response = await ojJudgeApi.task.detail({ id })
    state.task = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="720">
    <NDrawerContent title="判题任务详情" closable>
      <NSpin :show="state.loading">
        <NDescriptions v-if="state.task" :column="1" bordered size="small">
          <NDescriptionsItem label="id">{{ state.task.id }}</NDescriptionsItem>
          <NDescriptionsItem label="submission_id">{{ state.task.submission_id }}</NDescriptionsItem>
          <NDescriptionsItem label="problem_id">{{ state.task.problem_id }}</NDescriptionsItem>
          <NDescriptionsItem label="judge_node_id">{{ state.task.judge_node_id || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="task_type">{{ state.task.task_type }}</NDescriptionsItem>
          <NDescriptionsItem label="priority">{{ state.task.priority }}</NDescriptionsItem>
          <NDescriptionsItem label="status">{{ state.task.status }}</NDescriptionsItem>
          <NDescriptionsItem label="attempts">{{ state.task.attempts }}</NDescriptionsItem>
          <NDescriptionsItem label="locked_at">{{ formatDateTime(state.task.locked_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="started_at">{{ formatDateTime(state.task.started_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="finished_at">{{ formatDateTime(state.task.finished_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="error">{{ state.task.error || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="payload">
            <NCode :code="JSON.stringify(state.task.payload ?? {}, null, 2)" language="json" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="result_payload">
            <NCode :code="JSON.stringify(state.task.result_payload ?? {}, null, 2)" language="json" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="created_at">{{ formatDateTime(state.task.created_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="updated_at">{{ formatDateTime(state.task.updated_at) }}</NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
