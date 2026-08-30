<!-- Author: Charlie -->

<script setup lang="ts">
import { deptApi } from '@/api'
import { createTagColor, displayValue, formatDateTime, wireBool } from '@/utils'
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
  <HeiDetailContainer
    v-model:show="state.showModal"
    :title="'部门详情'"
    :width="680"
    :mask-closable="false"
  >
      <NSpin :show="state.loading">
        <NDescriptions
          label-placement="left"
          bordered
          :column="1"
        >
          <NDescriptionsItem :label="'部门 ID'">
            {{ displayValue(state.dept.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'部门名称'">
            {{ displayValue(state.dept.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'部门分类'">
            {{
              dictTypeData('DEPT_CATEGORY', state.dept.category) ||
                displayValue(state.dept.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'父级部门'">
            {{ displayValue(state.dept.parent_name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'负责人'">
            {{ displayValue(state.dept.master_name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'副负责人'">
            {{ displayValue(state.dept.deputy_master_name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'排序'">
            {{ displayValue(state.dept.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'虚拟部门'">
            {{ wireBool(state.dept.is_virtual) ? '是' : '否' }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'状态'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.dept.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.dept.status) || displayValue(state.dept.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'创建时间'">
            {{ formatDateTime(state.dept.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'更新时间'">
            {{ formatDateTime(state.dept.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
  </HeiDetailContainer>
</template>
