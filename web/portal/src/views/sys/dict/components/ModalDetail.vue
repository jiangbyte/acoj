<script setup lang="ts">
import { dictApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  data: {} as any,
})

async function openModal(id: string) {
  state.data = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await dictApi.detail({ id })
    state.data = response.data ?? {}
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
    :title="t('pages.sys.dict.detailDict')"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('pages.sys.dict.id')">
            {{ displayValue(state.data.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.code')">
            {{ displayValue(state.data.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.label')">
            {{ displayValue(state.data.label) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.value')">
            {{ displayValue(state.data.value) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.color')">
            <NTag
              v-if="state.data.color"
              :color="createTagColor(state.data.color)"
              :bordered="false"
            >
              {{ state.data.color }}
            </NTag>
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.category')">
            {{ dictTypeData('DICT_CATEGORY', state.data.category) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.parent')">
            {{ displayValue(state.data.parent_id_name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.sys.dict.sort')">
            {{ displayValue(state.data.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.data.status))"
              :bordered="false"
            >
              {{ dictTypeData('COMMON_STATUS', state.data.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdAt')">
            {{ displayValue(state.data.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdBy')">
            {{ displayValue(state.data.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedAt')">
            {{ displayValue(state.data.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedBy')">
            {{ displayValue(state.data.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
