<script setup lang="ts">
import { resourceApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  resource: {} as any,
})

async function openModal(id: string) {
  state.resource = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await resourceApi.detail({ id })
    state.resource = response.data ?? {}
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
    :title="'Resource Detail'"
    style="width: 720px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'Resource ID'">
            {{ displayValue(state.resource.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Resource Name'">
            {{ displayValue(state.resource.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Resource Code'">
            {{ displayValue(state.resource.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Resource Type'">
            {{
              dictTypeData('RESOURCE_TYPE', state.resource.resource_type) ||
              displayValue(state.resource.resource_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Parent Resource ID'">
            {{ displayValue(state.resource.parent_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Resource Module'">
            {{ displayValue(state.resource.module_id_name || state.resource.module_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Path'">
            {{ displayValue(state.resource.path) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Component'">
            {{ displayValue(state.resource.component) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Redirect'">
            {{ displayValue(state.resource.redirect) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Icon'">
            {{ displayValue(state.resource.icon) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Href'">
            {{ displayValue(state.resource.href) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Sort'">
            {{ displayValue(state.resource.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Visible'">
            {{
              state.resource.is_visible
                ? 'Yes'
                : 'No'
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Cache'">
            {{
              state.resource.is_cache
                ? 'Yes'
                : 'No'
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Affix'">
            {{
              state.resource.is_affix
                ? 'Yes'
                : 'No'
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.resource.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.resource.status) ||
                displayValue(state.resource.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Description'">
            {{ displayValue(state.resource.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.resource.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created By'">
            {{ displayValue(state.resource.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.resource.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated By'">
            {{ displayValue(state.resource.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
