<script setup lang="ts">
import { deptApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  dept: {} as any,
})

async function openModal(id: string) {
  state.dept = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await deptApi.detail({ id })
    state.dept = response.data ?? {}
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
    :title="'Department Detail'"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'Department ID'">
            {{ displayValue(state.dept.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Department Name'">
            {{ displayValue(state.dept.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Department Code'">
            {{ displayValue(state.dept.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Department Category'">
            {{
              dictTypeData('DEPT_CATEGORY', state.dept.category) ||
              displayValue(state.dept.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Parent Department ID'">
            {{ displayValue(state.dept.parent_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Master ID'">
            {{ displayValue(state.dept.master_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Deputy Master ID'">
            {{ displayValue(state.dept.deputy_master_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.dept.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Virtual Department'">
            {{ state.dept.is_virtual ? 'Yes' : 'No' }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.dept.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.dept.status) || displayValue(state.dept.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.dept.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.dept.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.dept.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
            {{ displayValue(state.dept.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
