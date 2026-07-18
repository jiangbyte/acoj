<script setup lang="ts">
import { ojJudgeApi } from '@/api'
import { formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  language: null as any,
})

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.language = null
  try {
    const response = await ojJudgeApi.language.detail({ id })
    state.language = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="760">
    <NDrawerContent title="语言配置详情" closable>
      <NSpin :show="state.loading">
        <NDescriptions v-if="state.language" :column="1" bordered size="small">
          <NDescriptionsItem label="id">{{ state.language.id }}</NDescriptionsItem>
          <NDescriptionsItem label="key">{{ state.language.key }}</NDescriptionsItem>
          <NDescriptionsItem label="name">{{ state.language.name }}</NDescriptionsItem>
          <NDescriptionsItem label="short_name">{{ state.language.short_name || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="common_name">{{ state.language.common_name || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="ace_mode">{{ state.language.ace_mode || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="pygments">{{ state.language.pygments || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="extension">{{ state.language.extension || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="template">
            <NCode :code="state.language.template || '-'" language="text" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="compile_command">
            <NCode :code="state.language.compile_command || '-'" language="text" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="run_command">
            <NCode :code="state.language.run_command || '-'" language="text" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="status">{{ state.language.status }}</NDescriptionsItem>
          <NDescriptionsItem label="extra">
            <NCode :code="JSON.stringify(state.language.extra ?? {}, null, 2)" language="json" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="created_at">{{ formatDateTime(state.language.created_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="updated_at">{{ formatDateTime(state.language.updated_at) }}</NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
