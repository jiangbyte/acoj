<script setup lang="ts">
import { messageApi } from '@/api'
import { createTagColor, displayValue, formatDateTime } from '@/utils'
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
    const response = await messageApi.todoDetail({ id })
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
    :title="'待办 详情'"
    style="width: 640px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'ID'">
            {{ displayValue(state.detailData.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'标题'">
            {{ displayValue(state.detailData.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'优先级'">
            <NTag
              :color="createTagColor(dictTypeColor('TODO_PRIORITY', state.detailData.priority))"
              :bordered="false"
            >
              {{
                dictTypeData('TODO_PRIORITY', state.detailData.priority) ||
                displayValue(state.detailData.priority)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'目标范围'">
            {{
              dictTypeData('MESSAGE_TARGET_SCOPE', state.detailData.target_scope) ||
              displayValue(state.detailData.target_scope)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'目标账号类型'">
            {{
              dictTypeData('ACCOUNT_TYPE', state.detailData.target_account_type) ||
              displayValue(state.detailData.target_account_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'目标账号ID'">
            {{ displayValue(state.detailData.target_account_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'状态'">
            <NTag
              :color="createTagColor(dictTypeColor('TODO_STATUS', state.detailData.status))"
              :bordered="false"
            >
              {{
                dictTypeData('TODO_STATUS', state.detailData.status) ||
                displayValue(state.detailData.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="'截止时间'">
            {{ formatDateTime(state.detailData.due_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'内容'">
            {{ displayValue(state.detailData.content) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'创建时间'">
            {{ formatDateTime(state.detailData.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'更新时间'">
            {{ formatDateTime(state.detailData.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
