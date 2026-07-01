<script setup lang="ts">
import { positionApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="t('resource.iam.position.detail_position')"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('resource.iam.position.id')">
            {{ displayValue(state.position.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.position.name')">
            {{ displayValue(state.position.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.position.code')">
            {{ displayValue(state.position.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.position.category')">
            {{
              dictTypeData('POSITION_CATEGORY', state.position.category) ||
              displayValue(state.position.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.position.sort')">
            {{ displayValue(state.position.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.position.is_virtual')">
            {{
              state.position.is_virtual ? t('resource.iam.position.yes') : t('resource.iam.position.no')
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
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
          <NDescriptionsItem :label="t('resource.iam.position.description')">
            {{ displayValue(state.position.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_at')">
            {{ displayValue(state.position.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_by')">
            {{ displayValue(state.position.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.position.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_by')">
            {{ displayValue(state.position.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
