<script setup lang="ts">
import { groupApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="t('pages.iam.group.detailGroup')"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('pages.iam.group.id')">
            {{ displayValue(state.group.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.group.name')">
            {{ displayValue(state.group.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
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
          <NDescriptionsItem :label="t('pages.iam.group.description')">
            {{ displayValue(state.group.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdAt')">
            {{ displayValue(state.group.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdBy')">
            {{ displayValue(state.group.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedAt')">
            {{ displayValue(state.group.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedBy')">
            {{ displayValue(state.group.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
