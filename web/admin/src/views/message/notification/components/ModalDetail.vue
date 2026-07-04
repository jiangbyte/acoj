<script setup lang="ts">
import { messageApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { reactive } from 'vue'

const state = reactive({
  showModal: false,
  loading: false,
  detailData: {} as any,
})

async function openModal(id: string) {
  state.detailData = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await messageApi.notificationDetail({ id })
    state.detailData = response.data ?? {}
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="'Notification Detail'"
    style="width: 640px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'ID'">
            {{ displayValue(state.detailData.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Title'">
            {{ displayValue(state.detailData.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Severity'">
            <NTag
              :color="
                createTagColor(dictTypeColor('NOTIFICATION_SEVERITY', state.detailData.severity))
              "
              :bordered="false"
            >
              {{
                dictTypeData('NOTIFICATION_SEVERITY', state.detailData.severity) ||
                displayValue(state.detailData.severity)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Target Scope'">
            {{
              dictTypeData('MESSAGE_TARGET_SCOPE', state.detailData.target_scope) ||
              displayValue(state.detailData.target_scope)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Target Account Type'">
            {{
              dictTypeData('ACCOUNT_TYPE', state.detailData.target_account_type) ||
              displayValue(state.detailData.target_account_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Target Account ID'">
            {{ displayValue(state.detailData.target_account_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Status'">
            <NTag
              :color="createTagColor(dictTypeColor('NOTIFICATION_STATUS', state.detailData.status))"
              :bordered="false"
            >
              {{
                dictTypeData('NOTIFICATION_STATUS', state.detailData.status) ||
                displayValue(state.detailData.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Published At'">
            {{ displayValue(state.detailData.publish_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Content'">
            {{ displayValue(state.detailData.content) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Created At'">
            {{ displayValue(state.detailData.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'Updated At'">
            {{ displayValue(state.detailData.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
