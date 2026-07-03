<script setup lang="ts">
import { resourceModuleApi } from '@/api'
import { createTagColor, displayValue, translateLocale } from '@/utils'
import { reactive } from 'vue'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  module: {} as any,
})

async function openModal(id: string) {
  state.module = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await resourceModuleApi.detail({ id })
    state.module = response.data ?? {}
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
    :title="t('resource.iam.resource_module.detail_module')"
    style="width: 620px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('resource.iam.resource_module.id')">
            {{ displayValue(state.module.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.name')">
            {{ displayValue(translateLocale(state.module.locale_key, state.module.name)) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.locale_key')">
            {{ displayValue(state.module.locale_key) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.code')">
            {{ displayValue(state.module.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.client')">
            {{
              dictTypeData('RESOURCE_MODULE_CLIENT', state.module.client) ||
              displayValue(state.module.client)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.icon')">
            {{ displayValue(state.module.icon) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.color')">
            <NTag
              v-if="state.module.color"
              :color="createTagColor(state.module.color)"
              :bordered="false"
            >
              {{ state.module.color }}
            </NTag>
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.sort')">
            {{ displayValue(state.module.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.module.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.module.status) ||
                displayValue(state.module.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource_module.description')">
            {{ displayValue(state.module.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_at')">
            {{ displayValue(state.module.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_by')">
            {{ displayValue(state.module.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.module.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_by')">
            {{ displayValue(state.module.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
