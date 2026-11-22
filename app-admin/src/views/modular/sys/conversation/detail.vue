<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import {
  NButton,
  NCard,
  NCode,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NEmpty,
  NModal,
  NPagination,
  NSpace,
  NSpin,
  NTag,
  NText,
  NTime,
} from 'naive-ui'
import { useSysConversationFetch } from '@/composables/v1'
import MDViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const sessionMeta = ref<any>(null)
const timeline = ref<any[]>([])
const toolRows = ref<any[]>([])
/** 已展开的消息 id */
const expandedMsgIds = ref<Set<string>>(new Set())
const toolDetailShow = ref(false)
const toolDetail = ref<any>(null)

const timelinePage = ref({ current: 1, size: 10, pages: 0, total: 0 })
const toolPage = ref({ current: 1, size: 10, pages: 0, total: 0 })
const timelineLoading = ref(false)
const toolLoading = ref(false)

const { conversationHistoryPage, toolTracePage } = useSysConversationFetch()

function doClose() {
  emit('close')
  show.value = false
  sessionMeta.value = null
  timeline.value = []
  toolRows.value = []
  expandedMsgIds.value = new Set()
  toolDetailShow.value = false
  toolDetail.value = null
  timelinePage.value = { current: 1, size: 10, pages: 0, total: 0 }
  toolPage.value = { current: 1, size: 10, pages: 0, total: 0 }
}

/**
 * 加载时间线分页。
 */
async function loadTimeline() {
  if (!sessionMeta.value?.conversationId) {
    return
  }
  timelineLoading.value = true
  try {
    const { data } = await conversationHistoryPage(sessionMeta.value.conversationId, {
      current: timelinePage.value.current,
      size: timelinePage.value.size,
    })
    timeline.value = data?.records || []
    timelinePage.value.pages = Number(data?.pages || 0)
    timelinePage.value.total = Number(data?.total || 0)
    expandedMsgIds.value = new Set()
  }
  finally {
    timelineLoading.value = false
  }
}

/**
 * 加载工具轨迹分页；按 conversationId，必要时回退 problemId+userId。
 */
async function loadToolTraces() {
  if (!sessionMeta.value?.conversationId) {
    return
  }
  toolLoading.value = true
  try {
    // 1. 先按 conversationId 查（修复后写入的轨迹）
    let { data } = await toolTracePage({
      current: toolPage.value.current,
      size: toolPage.value.size,
      conversationId: sessionMeta.value.conversationId,
    })
    // 2. 若为空且有 problem/user，再按题目+用户回退（兼容旧数据未绑 conversationId）
    if ((!data?.records || data.records.length === 0) && (sessionMeta.value.problemId || sessionMeta.value.userId)) {
      const fallback = await toolTracePage({
        current: toolPage.value.current,
        size: toolPage.value.size,
        problemId: sessionMeta.value.problemId || undefined,
        userId: sessionMeta.value.userId || undefined,
      })
      data = fallback.data
    }
    toolRows.value = data?.records || []
    toolPage.value.pages = Number(data?.pages || 0)
    toolPage.value.total = Number(data?.total || 0)
  }
  finally {
    toolLoading.value = false
  }
}

async function doOpen(row: any) {
  show.value = true
  sessionMeta.value = row
  loading.value = true
  timelinePage.value.current = 1
  toolPage.value.current = 1
  expandedMsgIds.value = new Set()
  try {
    await Promise.all([loadTimeline(), loadToolTraces()])
  }
  finally {
    loading.value = false
  }
}

function roleTagType(role: string) {
  return role === 'USER' ? 'info' : 'success'
}

function isExpanded(id: string) {
  return expandedMsgIds.value.has(id)
}

function toggleExpand(id: string) {
  const next = new Set(expandedMsgIds.value)
  if (next.has(id)) {
    next.delete(id)
  }
  else {
    next.add(id)
  }
  expandedMsgIds.value = next
}

function previewText(content: string | null | undefined) {
  if (!content) {
    return '(empty)'
  }
  const flat = String(content).replace(/\s+/g, ' ').trim()
  return flat.length > 100 ? `${flat.slice(0, 100)}...` : flat
}

function expandAll() {
  expandedMsgIds.value = new Set(timeline.value.map((m: any) => m.id).filter(Boolean))
}

function collapseAll() {
  expandedMsgIds.value = new Set()
}

function openToolDetail(row: any) {
  toolDetail.value = row
  toolDetailShow.value = true
}

function formatJsonLike(raw: string | null | undefined) {
  if (!raw) {
    return ''
  }
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  }
  catch {
    return raw
  }
}

async function onTimelinePageChange(page: number) {
  timelinePage.value.current = page
  await loadTimeline()
}

async function onTimelinePageSizeChange(size: number) {
  timelinePage.value.size = size
  timelinePage.value.current = 1
  await loadTimeline()
}

async function onToolPageChange(page: number) {
  toolPage.value.current = page
  await loadToolTraces()
}

async function onToolPageSizeChange(size: number) {
  toolPage.value.size = size
  toolPage.value.current = 1
  await loadToolTraces()
}

const toolColumns: DataTableColumns<any> = [
  {
    title: 'Tool',
    key: 'toolName',
    width: 140,
    ellipsis: { tooltip: true },
  },
  {
    title: 'Status',
    key: 'status',
    width: 90,
    render(row) {
      const err = row.status === 'ERROR'
      return h(NTag, { type: err ? 'error' : 'success', size: 'small' }, { default: () => row.status || '-' })
    },
  },
  {
    title: 'Time',
    key: 'createTime',
    width: 170,
    render(row) {
      if (!row.createTime) {
        return h(NText, { depth: 3 }, { default: () => '-' })
      }
      return h(NTime, { time: Number(row.createTime) })
    },
  },
  {
    title: 'Duration',
    key: 'durationMs',
    width: 90,
    render(row) {
      return row.durationMs != null && row.durationMs !== '' ? `${row.durationMs} ms` : '-'
    },
  },
  {
    title: 'Args',
    key: 'toolArgs',
    ellipsis: { tooltip: true },
    render(row) {
      return row.toolArgs ? previewText(row.toolArgs) : h(NText, { depth: 3 }, { default: () => '-' })
    },
  },
  {
    title: 'Result',
    key: 'toolResult',
    ellipsis: { tooltip: true },
    render(row) {
      return row.toolResult ? previewText(row.toolResult) : h(NText, { depth: 3 }, { default: () => '-' })
    },
  },
  {
    title: 'Actions',
    key: 'action',
    width: 80,
    fixed: 'right',
    render(row) {
      return h(
        NButton,
        {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: () => openToolDetail(row),
        },
        { default: () => 'View' },
      )
    },
  },
]

defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="880" @after-leave="doClose">
    <NDrawerContent title="Session Details" :native-scrollbar="false">
      <NSpin :show="loading">
        <NSpace vertical :size="16" class="w-full">
          <NDescriptions v-if="sessionMeta" label-placement="left" bordered :column="2" size="small">
            <NDescriptionsItem label="Conversation ID" :span="2">
              {{ sessionMeta.conversationId }}
            </NDescriptionsItem>
            <NDescriptionsItem label="User">
              <NSpace align="center" size="small">
                <SafeAvatar
                  size="small"
                  :round="true"
                  :src="sessionMeta.userAvatar"
                />
                <NText>{{ sessionMeta.userIdName }}</NText>
              </NSpace>
            </NDescriptionsItem>
            <NDescriptionsItem label="Problem">
              {{ sessionMeta.problemIdName || '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Messages">
              {{ sessionMeta.messageCount }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Status">
              <NTag :type="sessionMeta.status === 'ERROR' ? 'error' : 'success'" size="small">
                {{ sessionMeta.status || 'SUCCESS' }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="Tokens">
              {{ sessionMeta.totalTokens || 0 }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Duration">
              {{ sessionMeta.totalDurationMs ? `${sessionMeta.totalDurationMs} ms` : '-' }}
            </NDescriptionsItem>
            <NDescriptionsItem label="Started">
              <NTime v-if="sessionMeta.startedAt" :time="Number(sessionMeta.startedAt)" />
            </NDescriptionsItem>
            <NDescriptionsItem label="Last Active">
              <NTime v-if="sessionMeta.lastActiveAt" :time="Number(sessionMeta.lastActiveAt)" />
            </NDescriptionsItem>
          </NDescriptions>

          <div>
            <NSpace align="center" justify="space-between" class="mb-2">
              <NText strong>
                Timeline
              </NText>
              <NSpace v-if="timeline.length > 0" size="small">
                <NButton size="tiny" quaternary @click="expandAll">
                  Expand all
                </NButton>
                <NButton size="tiny" quaternary @click="collapseAll">
                  Collapse all
                </NButton>
              </NSpace>
            </NSpace>
            <NSpin :show="timelineLoading">
              <NEmpty v-if="!timelineLoading && timeline.length === 0" description="No messages" class="py-8" />
              <NSpace v-else vertical :size="8">
                <NCard
                  v-for="msg in timeline"
                  :key="msg.id"
                  size="small"
                  class="timeline-card cursor-pointer"
                  :class="msg.messageRole === 'USER' ? 'bg-#f0f7ff' : ''"
                  hoverable
                  @click="toggleExpand(msg.id)"
                >
                  <NSpace vertical :size="6">
                    <NSpace align="center" justify="space-between">
                      <NSpace align="center" size="small">
                        <NText depth="3" class="text-xs w-4">
                          {{ isExpanded(msg.id) ? '▼' : '▶' }}
                        </NText>
                        <NTag :type="roleTagType(msg.messageRole)" size="small">
                          {{ msg.messageRoleName || msg.messageRole }}
                        </NTag>
                        <NTag size="small">
                          {{ msg.messageTypeName || msg.messageType }}
                        </NTag>
                        <NTag v-if="msg.status" :type="msg.status === 'ERROR' ? 'error' : 'default'" size="small">
                          {{ msg.status }}
                        </NTag>
                      </NSpace>
                      <NTime v-if="msg.createTime || msg.responseTime" :time="Number(msg.createTime || msg.responseTime)" />
                    </NSpace>

                    <NText v-if="!isExpanded(msg.id)" depth="3" class="timeline-preview">
                      {{ previewText(msg.messageContent) }}
                      <NText v-if="msg.errorMessage" type="error">
                        · {{ previewText(msg.errorMessage) }}
                      </NText>
                    </NText>

                    <template v-else>
                      <MDViewer v-if="msg.messageContent" :model-value="msg.messageContent" />
                      <NText v-else depth="3">
                        (empty content)
                      </NText>
                      <NText v-if="msg.errorMessage" type="error" depth="3">
                        {{ msg.errorMessage }}
                      </NText>
                      <NSpace v-if="msg.streamingDuration || msg.totalTokens" size="small">
                        <NText v-if="msg.streamingDuration" depth="3" class="text-xs">
                          Duration: {{ msg.streamingDuration }} ms
                        </NText>
                        <NText v-if="msg.totalTokens" depth="3" class="text-xs">
                          Tokens: {{ msg.totalTokens }}
                        </NText>
                      </NSpace>
                    </template>
                  </NSpace>
                </NCard>
              </NSpace>
              <NSpace v-if="timelinePage.total > 0" justify="end" class="mt-3">
                <NPagination
                  v-model:page="timelinePage.current"
                  v-model:page-size="timelinePage.size"
                  :page-count="timelinePage.pages"
                  :item-count="timelinePage.total"
                  show-size-picker
                  :page-sizes="[10, 20, 50]"
                  @update:page="onTimelinePageChange"
                  @update:page-size="onTimelinePageSizeChange"
                />
              </NSpace>
            </NSpin>
          </div>

          <div>
            <NText strong>
              Tool Traces
            </NText>
            <NSpin :show="toolLoading">
              <NEmpty
                v-if="!toolLoading && toolRows.length === 0"
                class="py-6"
                description="No tool calls in this session"
              >
                <template #extra>
                  <NText depth="3" class="text-xs">
                    Tools are recorded when the model invokes them (e.g. fetch description by problemId). Older rows without conversationId are matched by session time window.
                  </NText>
                </template>
              </NEmpty>
              <template v-else-if="toolRows.length > 0">
                <NDataTable
                  class="mt-2"
                  :columns="toolColumns"
                  :data="toolRows"
                  :pagination="false"
                  :scroll-x="900"
                  size="small"
                  :row-key="(row: any) => row.id"
                />
                <NSpace justify="end" class="mt-3">
                  <NPagination
                    v-model:page="toolPage.current"
                    v-model:page-size="toolPage.size"
                    :page-count="toolPage.pages"
                    :item-count="toolPage.total"
                    show-size-picker
                    :page-sizes="[10, 20, 50]"
                    @update:page="onToolPageChange"
                    @update:page-size="onToolPageSizeChange"
                  />
                </NSpace>
              </template>
            </NSpin>
          </div>
        </NSpace>
      </NSpin>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>

  <NModal
    v-model:show="toolDetailShow"
    preset="card"
    title="Tool Trace Detail"
    class="w-720px fixed-height-modal"
  >
    <div class="modal-scroll-body">
      <NDescriptions v-if="toolDetail" label-placement="left" bordered :column="1" size="small">
        <NDescriptionsItem label="Tool">
          {{ toolDetail.toolName || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Conversation">
          {{ toolDetail.conversationId || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Status">
          <NTag :type="toolDetail.status === 'ERROR' ? 'error' : 'success'" size="small">
            {{ toolDetail.status || '-' }}
          </NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="Duration">
          {{ toolDetail.durationMs != null ? `${toolDetail.durationMs} ms` : '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Time">
          <NTime v-if="toolDetail.createTime" :time="Number(toolDetail.createTime)" />
          <NText v-else depth="3">
            -
          </NText>
        </NDescriptionsItem>
        <NDescriptionsItem label="Error">
          {{ toolDetail.errorMessage || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Args">
          <NCode v-if="toolDetail.toolArgs" :code="formatJsonLike(toolDetail.toolArgs)" language="json" word-wrap />
          <NText v-else depth="3">
            -
          </NText>
        </NDescriptionsItem>
        <NDescriptionsItem label="Result">
          <NCode v-if="toolDetail.toolResult" :code="formatJsonLike(toolDetail.toolResult)" language="json" word-wrap />
          <NText v-else depth="3">
            -
          </NText>
        </NDescriptionsItem>
      </NDescriptions>
    </div>
  </NModal>
</template>

<style scoped>
.timeline-preview {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.timeline-card :deep(.n-card__content) {
  padding: 10px 12px;
}

.modal-scroll-body {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 4px;
}
</style>

<style>
/* NModal teleport 到 body，需非 scoped */
.fixed-height-modal.n-card {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.fixed-height-modal.n-card .n-card__content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
