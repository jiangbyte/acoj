<script setup lang="ts">
import { ojJudgeApi } from '@/api'
import { formatDateTime } from '@/utils'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  node: null as any,
})

async function openModal(id: string) {
  state.showModal = true
  state.loading = true
  state.node = null
  try {
    const response = await ojJudgeApi.node.detail({ id })
    state.node = response.data
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NDrawer v-model:show="state.showModal" :width="720">
    <NDrawerContent title="判题节点详情" closable>
      <NSpin :show="state.loading">
        <NDescriptions v-if="state.node" :column="1" bordered size="small">
          <NDescriptionsItem label="id">{{ state.node.id }}</NDescriptionsItem>
          <NDescriptionsItem label="name">{{ state.node.name }}</NDescriptionsItem>
          <NDescriptionsItem label="auth_key_hash">{{ state.node.auth_key_hash }}</NDescriptionsItem>
          <NDescriptionsItem label="status">{{ state.node.status }}</NDescriptionsItem>
          <NDescriptionsItem label="online">{{ state.node.online }}</NDescriptionsItem>
          <NDescriptionsItem label="tier">{{ state.node.tier }}</NDescriptionsItem>
          <NDescriptionsItem label="last_ip">{{ state.node.last_ip || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="last_heartbeat_at">{{ formatDateTime(state.node.last_heartbeat_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="load">{{ state.node.load ?? '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="supported_languages">
            <NCode :code="JSON.stringify(state.node.supported_languages ?? [], null, 2)" language="json" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="supported_modes">
            <NCode :code="JSON.stringify(state.node.supported_modes ?? [], null, 2)" language="json" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="description">{{ state.node.description || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="extra">
            <NCode :code="JSON.stringify(state.node.extra ?? {}, null, 2)" language="json" word-wrap />
          </NDescriptionsItem>
          <NDescriptionsItem label="created_at">{{ formatDateTime(state.node.created_at) }}</NDescriptionsItem>
          <NDescriptionsItem label="updated_at">{{ formatDateTime(state.node.updated_at) }}</NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NDrawerContent>
  </NDrawer>
</template>
