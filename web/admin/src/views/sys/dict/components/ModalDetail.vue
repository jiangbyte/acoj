<script setup lang="ts">
import { dictApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  data: {} as any,
})

async function openModal(id: string) {
  state.data = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await dictApi.detail({ id })
    state.data = response.data ?? {}
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
    :title="'Dict Detail'"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'ID'">
            {{ displayValue(state.data.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Code'">
            {{ displayValue(state.data.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Label'">
            {{ displayValue(state.data.label) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Value'">
            {{ displayValue(state.data.value) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Color'">
            <NTag
              v-if="state.data.color"
              :color="createTagColor(state.data.color)"
              :bordered="false"
            >
              {{ state.data.color }}
            </NTag>
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Category'">
            {{ dictTypeData('SYS_BIZ_CATEGORY', state.data.category) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Parent Dict'">
            {{ displayValue(state.data.parent_id_name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.data.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.data.status))"
              :bordered="false"
            >
              {{ dictTypeData('COMMON_STATUS', state.data.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.data.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.data.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.data.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
            {{ displayValue(state.data.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
