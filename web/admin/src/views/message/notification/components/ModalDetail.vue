<script setup lang="ts">
import { messageApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
    :title="t('resource.message.notification.detail_notification')"
    style="width: 640px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('common.often.index')">
            {{ displayValue(state.detailData.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.message.notification.title_field')">
            {{ displayValue(state.detailData.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.message.notification.severity')">
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
          <NDescriptionsItem :label="t('resource.message.notification.target_scope')">
            {{
              dictTypeData('MESSAGE_TARGET_SCOPE', state.detailData.target_scope) ||
              displayValue(state.detailData.target_scope)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.message.notification.target_account_type')">
            {{
              dictTypeData('ACCOUNT_TYPE', state.detailData.target_account_type) ||
              displayValue(state.detailData.target_account_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.message.notification.target_account_id')">
            {{ displayValue(state.detailData.target_account_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
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
          <NDescriptionsItem :label="t('resource.message.notification.publish_at')">
            {{ displayValue(state.detailData.publish_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('resource.message.notification.content')">
            {{ displayValue(state.detailData.content) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.created_at')">
            {{ displayValue(state.detailData.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.detailData.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
