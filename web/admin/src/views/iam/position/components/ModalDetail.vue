<script setup lang="ts">
import { positionApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  position: {} as any,
})

async function openModal(id: string) {
  state.position = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await positionApi.detail({ id })
    state.position = response.data ?? {}
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
    :title="'Position Detail'"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'Position ID'">
            {{ displayValue(state.position.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Position Name'">
            {{ displayValue(state.position.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Position Code'">
            {{ displayValue(state.position.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Position Category'">
            {{
              dictTypeData('POSITION_CATEGORY', state.position.category) ||
              displayValue(state.position.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.position.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Virtual Position'">
            {{
              state.position.is_virtual
                ? 'Yes'
                : 'No'
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.position.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.position.status) ||
                displayValue(state.position.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Description'">
            {{ displayValue(state.position.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.position.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.position.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.position.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
            {{ displayValue(state.position.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
