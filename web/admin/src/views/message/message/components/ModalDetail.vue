<script setup lang="ts">
import { messageApi } from '@/api'
import { displayValue, formatDateTime, resolveFileUrl } from '@/utils'
import { dictTypeData } from '@/utils/dict'
import { reactive, ref } from 'vue'

const activeTab = ref<'threads' | 'groups'>('threads')
const state = reactive({
  showModal: false,
  loading: false,
  detailData: {} as any,
  detailMessage: [] as any[],
})

async function openModal(row: any, tab: 'threads' | 'groups') {
  activeTab.value = tab
  state.detailData = row
  state.detailMessage = []
  state.showModal = true
  await fetchDetail(row)
}

async function fetchDetail(row: any) {
  state.loading = true
  try {
    if (activeTab.value === 'threads') {
      const response = await messageApi.threadMessage({
        thread_id: row.id,
        current: 1,
        size: 10,
      })
      state.detailMessage = response.data?.records ?? []
    } else {
      const response = await messageApi.groupDetail({ id: row.id })
      state.detailData = response.data ?? row
    }
  } finally {
    state.loading = false
  }
}

defineExpose({ openModal })

function openAttachment(url: string) {
  const resolved = resolveFileUrl(url)
  if (!resolved) {
    return
  }
  window.open(resolved, '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="'消息 详情'"
    style="width: 720px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="'ID'">
            {{ displayValue(state.detailData.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'threads'"
            :label="'会话标题'"
          >
            {{ displayValue(state.detailData.title) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'threads'"
            :label="'会话类型'"
          >
            {{
              dictTypeData('MESSAGE_THREAD_TYPE', state.detailData.thread_type) ||
              displayValue(state.detailData.thread_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'groups'"
            :label="'用户组名称'"
          >
            {{ displayValue(state.detailData.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="activeTab === 'groups'"
            :label="'成员数'"
          >
            {{ displayValue(state.detailData.member_count) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'状态'">
            {{ displayValue(state.detailData.status) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="'更新时间'">
            {{ formatDateTime(state.detailData.updated_at) }}
          </NDescriptionsItem>
        </NDescriptions>

        <NDivider v-if="activeTab === 'threads'" />
        <NList v-if="activeTab === 'threads' && state.detailMessage.length" bordered>
          <NListItem v-for="item in state.detailMessage" :key="item.id">
            <NThing>
              <template #header>
                {{ item.sender_name || item.sender_account_id || '系统' }}
              </template>
              <template #description>
                {{ formatDateTime(item.created_at) }}
              </template>
              {{ item.content }}
              <template v-if="item.attachments?.length" #footer>
                <NFlex class="mt-8px">
                  <NButton
                    v-for="attachment in item.attachments"
                    :key="attachment.id || attachment.url"
                    size="small"
                    @click="openAttachment(attachment.url)"
                  >
                    {{ attachment.name }}
                  </NButton>
                </NFlex>
              </template>
            </NThing>
          </NListItem>
        </NList>
        <NEmpty
          v-else-if="activeTab === 'threads'"
          class="py-32px"
          :description="'暂无消息'"
        />
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
