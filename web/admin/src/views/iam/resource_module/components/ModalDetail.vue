<script setup lang="ts">
import { resourceModuleApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  module: {} as any,
})

async function openModal(id: string) {
  state.module = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await resourceModuleApi.detail({ id })
    state.module = response.data ?? {}
  } finally {
    state.loading = false
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="'Resource Module Detail'"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'Resource Module ID'">
            {{ displayValue(state.module.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Module Name'">
            {{ displayValue(state.module.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Module Code'">
            {{ displayValue(state.module.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Client'">
            {{
              dictTypeData('RESOURCE_MODULE_CLIENT', state.module.client) ||
              displayValue(state.module.client)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Icon'">
            {{ displayValue(state.module.icon) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Color'">
            <NTag
              v-if="state.module.color"
              :color="createTagColor(state.module.color)"
              :bordered="false"
            >
              {{ state.module.color }}
            </NTag>
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.module.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.module.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.module.status) ||
                displayValue(state.module.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Description'">
            {{ displayValue(state.module.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.module.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.module.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.module.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
            {{ displayValue(state.module.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
