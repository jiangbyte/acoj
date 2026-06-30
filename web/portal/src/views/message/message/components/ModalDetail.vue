<script setup lang="ts">
import { messageApi } from '@/api'
import { displayValue } from '@/utils'
import { dictTypeData } from '@/utils/dict'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const activeTab = ref<'threads' | 'groups'>('threads')
const state = reactive({
  showModal: false,
  loading: false,
  detailData: {} as any,
  detailMessages: [] as any[],
})

async function openModal(row: any, tab: 'threads' | 'groups') {
  activeTab.value = tab
  state.detailData = row
  state.detailMessages = []
  state.showModal = true
  await fetchDetail(row)
}

async function fetchDetail(row: any) {
  state.loading = true
  try {
    if (activeTab.value === 'threads') {
      const response = await messageApi.threadMessages({
        thread_id: row.id,
        current: 1,
        size: 10,
      })
      state.detailMessages = response.data?.records ?? []
    } else {
      const response = await messageApi.groupDetail({ id: row.id })
      state.detailData = response.data ?? row
    }
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
    :title="t('resource.message.message.detail_message')"
    style="width: 720px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('common.often.index')">
            {{ displayValue(state.detailData.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'threads'"
            :label="t('resource.message.message.thread_title')"
          >
            {{ displayValue(state.detailData.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'threads'"
            :label="t('resource.message.message.thread_type')"
          >
            {{ dictTypeData('MESSAGE_THREAD_TYPE', state.detailData.thread_type) || displayValue(state.detailData.thread_type) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'groups'"
            :label="t('resource.message.message.group_name')"
          >
            {{ displayValue(state.detailData.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'groups'"
            :label="t('resource.message.message.member_count')"
          >
            {{ displayValue(state.detailData.member_count) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            {{ displayValue(state.detailData.status) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updated_at')">
            {{ displayValue(state.detailData.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>

        <NDivider v-if="activeTab === 'threads'" />
        <NList v-if="activeTab === 'threads' && state.detailMessages.length" bordered>
          <NListItem v-for="item in state.detailMessages" :key="item.id">
            <NThing>
              <template #header>
                {{ item.sender_name || item.sender_account_id || 'System' }}
              </template>
              <template #description>
                {{ item.created_at }}
              </template>
              {{ item.content }}
            </NThing>
          </NListItem>
        </NList>
        <NEmpty
          v-else-if="activeTab === 'threads'"
          class="py-32px"
          :description="t('resource.message.message.no_messages')"
        />
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
