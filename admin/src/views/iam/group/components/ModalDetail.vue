<!-- Author: Charlie -->

<script setup lang="ts">
import { groupApi } from '@/api'
import { createTagColor, displayValue, formatDateTime } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  group: {} as any,
})

async function openModal(id: string) {
  state.group = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await groupApi.detail({ id })
    state.group = response.data ?? {}
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
    :title="'用户组详情'"
    :width="680"
    :mask-closable="false"
  >
      <NSpin :show="state.loading">
        <NDescriptions
          label-placement="left"
          bordered
          :column="1"
        >
          <NDescriptionsItem :label="'用户组ID'">
            {{ displayValue(state.group.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'用户组名称'">
            {{ displayValue(state.group.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'状态'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.group.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.group.status) ||
                  displayValue(state.group.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'描述'">
            {{ displayValue(state.group.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'创建时间'">
            {{ formatDateTime(state.group.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'更新时间'">
            {{ formatDateTime(state.group.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
  </HeiDetailContainer>
</template>
