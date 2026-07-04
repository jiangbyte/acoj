<script setup lang="ts">
import { roleApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  role: {} as any,
})

async function openModal(id: string) {
  state.role = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await roleApi.detail({ id })
    state.role = response.data ?? {}
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
    :title="'Role Detail'"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'Role ID'">
            {{ displayValue(state.role.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Role Code'">
            {{ displayValue(state.role.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Role Name'">
            {{ displayValue(state.role.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Role Category'">
            {{
              dictTypeData('SYS_BIZ_CATEGORY', state.role.category) ||
              displayValue(state.role.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Scope Type'">
            {{
              dictTypeData('ROLE_SCOPE_TYPE', state.role.scope_type) ||
              displayValue(state.role.scope_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Owner Department ID'">
            {{ displayValue(state.role.owner_dept_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.role.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Builtin Role'">
            {{ state.role.is_builtin ? 'Yes' : 'No' }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.role.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.role.status) || displayValue(state.role.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Description'">
            {{ displayValue(state.role.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.role.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.role.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.role.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
            {{ displayValue(state.role.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
