<!--
  Author: Charlie

  OJ 标签详情弹窗。
-->
<script setup lang="ts">
import { ojTagApi } from '@/api'
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
    const response = await ojTagApi.detail({ id })
    state.detail = response.data ?? {}
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
    title="标签详情"
    :width="560"
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
        <NDescriptionsItem label="名称">
          {{ displayValue(state.detail.name) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="状态">
          <NTag
            :color="createTagColor(dictTypeColor('COMMON_STATUS', state.detail.status))"
            :bordered="false"
          >
            {{
              dictTypeData('COMMON_STATUS', state.detail.status) ||
                displayValue(state.detail.status)
            }}
          </NTag>
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
