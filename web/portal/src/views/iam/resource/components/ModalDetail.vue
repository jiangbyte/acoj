<script setup lang="ts">
import { resourceApi } from '@/api'
import { createTagColor, displayValue, translateLocale } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
    :title="t('resource.iam.resource.detail_resource')"
    style="width: 720px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('resource.iam.resource.id')">
            {{ displayValue(state.resource.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.name')">
            {{ displayValue(translateLocale(state.resource.locale_key, state.resource.name)) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.locale_key')">
            {{ displayValue(state.resource.locale_key) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.code')">
            {{ displayValue(state.resource.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.resource_type')">
            {{
              dictTypeData('RESOURCE_TYPE', state.resource.resource_type) ||
              displayValue(state.resource.resource_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.parent_id')">
            {{ displayValue(state.resource.parent_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.module')">
            {{ displayValue(state.resource.module_id_name || state.resource.module_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.path')">
            {{ displayValue(state.resource.path) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.component')">
            {{ displayValue(state.resource.component) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.redirect')">
            {{ displayValue(state.resource.redirect) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.icon')">
            {{ displayValue(state.resource.icon) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.href')">
            {{ displayValue(state.resource.href) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.sort')">
            {{ displayValue(state.resource.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.is_visible')">
            {{
              state.resource.is_visible ? t('resource.iam.resource.yes') : t('resource.iam.resource.no')
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.is_cache')">
            {{ state.resource.is_cache ? t('resource.iam.resource.yes') : t('resource.iam.resource.no') }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.iam.resource.is_affix')">
            {{ state.resource.is_affix ? t('resource.iam.resource.yes') : t('resource.iam.resource.no') }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
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
          <NDescriptionsItem :label="t('resource.iam.resource.description')">
            {{ displayValue(state.resource.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_at')">
            {{ displayValue(state.resource.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_by')">
            {{ displayValue(state.resource.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.resource.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_by')">
            {{ displayValue(state.resource.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
