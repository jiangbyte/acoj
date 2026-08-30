<!-- Author: Charlie -->

<script setup lang="ts">
import { positionApi } from '@/api'
import { createTagColor, displayValue, formatDateTime, wireBool } from '@/utils'
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
  <HeiDetailContainer
    v-model:show="state.showModal"
    :title="'岗位详情'"
    :width="680"
    :mask-closable="false"
  >
      <NSpin :show="state.loading">
        <NDescriptions
          label-placement="left"
          bordered
          :column="1"
        >
          <NDescriptionsItem :label="'岗位 ID'">
            {{ displayValue(state.position.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'岗位名称'">
            {{ displayValue(state.position.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'岗位分类'">
            {{
              dictTypeData('POSITION_CATEGORY', state.position.category) ||
                displayValue(state.position.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'排序'">
            {{ displayValue(state.position.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'虚拟岗位'">
            {{ wireBool(state.position.is_virtual) ? '是' : '否' }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'状态'">
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
          <NDescriptionsItem :label="'描述'">
            {{ displayValue(state.position.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'创建时间'">
            {{ formatDateTime(state.position.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'更新时间'">
            {{ formatDateTime(state.position.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
  </HeiDetailContainer>
</template>
