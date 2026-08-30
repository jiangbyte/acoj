<!--
  Author: Charlie

  消息/公告详情弹窗：公告态贴近运营弹窗（喇叭标题、富文本正文、暂时关闭 / 不再提示）。
-->
<script setup lang="ts">
import { Icon } from '@iconify/vue/offline'
import { MdPreview, RichTextPreview } from '@/components/editor'
import { myNoticeApi } from '@/api'
import { useMessageUnreadStore } from '@/stores'
import { displayValue, formatDateTime, wireBool } from '@/utils'
import { dictTypeData } from '@/utils/dict'
import { computed, reactive } from 'vue'

type MessageKind = 'NOTIFICATION' | 'ANNOUNCEMENT'
type OpenMode = 'detail' | 'popup'

export type MessageOpenOptions = {
  /** detail：点开阅读；popup：登录后强制弹窗 */
  mode?: OpenMode
  /** 打开时是否调用 my-detail（会顺带已读）；popup 默认 false */
  markReadOnOpen?: boolean
}

const emit = defineEmits<{
  changed: [payload: { type: string; id: string }]
  closed: [payload: { id: string; dismissed: boolean }]
}>()

const unreadStore = useMessageUnreadStore()

const state = reactive({
  show: false,
  loading: false,
  actionLoading: false,
  mode: 'detail' as OpenMode,
  source: {} as any,
  detail: {} as any,
  readLocally: false,
})

function resolveKind(raw: unknown): MessageKind {
  return String(raw || 'NOTIFICATION').toUpperCase() === 'ANNOUNCEMENT'
    ? 'ANNOUNCEMENT'
    : 'NOTIFICATION'
}

function asReadFlag(value: unknown): boolean {
  if (typeof value === 'boolean' || typeof value === 'string') {
    return wireBool(value)
  }
  return false
}

const messageKind = computed<MessageKind>(() =>
  resolveKind(state.detail.kind || state.source.sourceType || state.source.type),
)

const isAnnouncement = computed(() => messageKind.value === 'ANNOUNCEMENT')

/** 仅主动弹窗模式用运营弹窗壳；工作台/通知中心点开仍是普通详情 */
const isPopup = computed(() => state.mode === 'popup')

const kindLabel = computed(() => (isAnnouncement.value ? '公告' : '通知'))

const titleText = computed(() => displayValue(state.detail.title || state.source.title))

const headerTitle = computed(() =>
  isPopup.value ? '您有一条新消息啦！' : kindLabel.value,
)

const contentText = computed(() => displayValue(state.detail.content || state.source.content))

const contentType = computed(() =>
  String(state.detail.content_type || state.source.content_type || 'text').toLowerCase(),
)

const publishText = computed(() =>
  formatDateTime(state.detail.publish_at || state.source.publish_at || state.detail.created_at),
)

const severityLabel = computed(() => {
  const severity = state.detail.severity || state.source.severity
  if (!severity) return ''
  return dictTypeData('NOTIFICATION_SEVERITY', severity) || severity
})

const severityAlertType = computed(() => {
  const severity = String(state.detail.severity || state.source.severity || 'INFO').toUpperCase()
  if (severity === 'ERROR' || severity === 'DANGER') return 'error'
  if (severity === 'WARNING') return 'warning'
  if (severity === 'SUCCESS') return 'success'
  return 'info'
})

const isRead = computed(
  () => state.readLocally || asReadFlag(state.detail.is_read) || asReadFlag(state.source.is_read),
)

async function open(source: any, options: MessageOpenOptions = {}) {
  const mode = options.mode ?? 'detail'
  const markReadOnOpen = options.markReadOnOpen ?? mode === 'detail'
  const wasUnread = !asReadFlag(source?.is_read)

  state.mode = mode
  state.source = source ?? {}
  state.detail = { ...(source ?? {}) }
  state.readLocally = false
  state.show = true
  state.loading = true

  try {
    if (markReadOnOpen && state.source.id) {
      const response = await myNoticeApi.myDetail(state.source.id)
      state.detail = response.data ?? state.detail
      if (wasUnread) {
        markLocalRead()
        emit('changed', {
          type: resolveKind(state.detail.kind || messageKind.value),
          id: state.detail.id || state.source.id,
        })
        void unreadStore.refresh()
      }
    } else if (state.source.id && !state.source.content) {
      const response = await myNoticeApi.myDetail(state.source.id)
      state.detail = response.data ?? state.detail
      if (wasUnread) {
        markLocalRead()
        emit('changed', {
          type: resolveKind(state.detail.kind || messageKind.value),
          id: state.detail.id || state.source.id,
        })
        void unreadStore.refresh()
      }
    }
  } finally {
    state.loading = false
  }
}

function markLocalRead() {
  state.detail.is_read = true
  state.source.is_read = true
  state.readLocally = true
  unreadStore.notifyRead()
}

async function dismissForever() {
  const id = state.detail.id || state.source.id
  if (!id) {
    close(true)
    return
  }
  const kind = resolveKind(state.detail.kind || messageKind.value)
  state.actionLoading = true
  try {
    if (!isRead.value) {
      await myNoticeApi.read({ ids: [id] })
      markLocalRead()
      emit('changed', { type: kind, id })
      void unreadStore.refresh()
    }
    close(true)
  } finally {
    state.actionLoading = false
  }
}

function closeTemporarily() {
  close(false)
}

function close(dismissed: boolean) {
  const id = String(state.detail.id || state.source.id || '')
  state.show = false
  if (id) {
    emit('closed', { id, dismissed })
  }
}

function handleUpdateShow(show: boolean) {
  if (!show && state.show) {
    close(false)
    return
  }
  state.show = show
}

defineExpose({ open })
</script>

<template>
  <NModal
    :show="state.show"
    preset="card"
    :mask-closable="state.mode !== 'popup'"
    :closable="true"
    :bordered="false"
    size="small"
    style="width: min(640px, calc(100vw - 32px))"
    :segmented="{ content: true, footer: true }"
    @update:show="handleUpdateShow"
  >
    <template #header>
      <div class="msg-header">
        <NIcon
          :size="18"
          class="msg-header__icon"
        >
          <Icon icon="icon-park-outline:volume-notice" />
        </NIcon>
        <span class="msg-header__title">{{ headerTitle }}</span>
        <NBadge
          v-if="!isRead"
          dot
          type="error"
          processing
        />
      </div>
    </template>

    <NSpin :show="state.loading">
      <div class="msg-body">
        <NAlert
          v-if="titleText"
          :type="severityAlertType"
          :bordered="false"
          class="msg-alert"
        >
          {{ titleText }}
        </NAlert>

        <div
          v-if="publishText || severityLabel"
          class="msg-meta"
        >
          <NTag
            v-if="severityLabel"
            size="small"
            :bordered="false"
          >
            {{ severityLabel }}
          </NTag>
          <span v-if="publishText">{{ publishText }}</span>
        </div>

        <NScrollbar style="max-height: min(420px, calc(100vh - 260px))">
          <div class="msg-content">
            <div
              v-if="contentType === 'text'"
              class="msg-content__text"
            >
              {{ contentText }}
            </div>
            <MdPreview
              v-else-if="contentType === 'markdown'"
              :value="contentText"
            />
            <RichTextPreview
              v-else
              :value="contentText"
            />
          </div>
        </NScrollbar>
      </div>
    </NSpin>

    <template #footer>
      <div class="msg-footer">
        <template v-if="isPopup">
          <NButton @click="closeTemporarily">
            稍后提醒
          </NButton>
          <NButton
            type="primary"
            :loading="state.actionLoading"
            @click="dismissForever"
          >
            不再提示
          </NButton>
        </template>
        <template v-else>
          <NButton
            v-if="!isRead"
            secondary
            :loading="state.actionLoading"
            @click="dismissForever"
          >
            标记已读
          </NButton>
          <NButton
            type="primary"
            @click="closeTemporarily"
          >
            关闭
          </NButton>
        </template>
      </div>
    </template>
  </NModal>
</template>

<style scoped>
.msg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.msg-header__icon {
  color: var(--primary-color, #1677ff);
  flex-shrink: 0;
}

.msg-header__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color-1, #1f1f1f);
  line-height: 1.3;
}

.msg-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.msg-alert {
  border-radius: 4px;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-color-3, #8b95a5);
}

.msg-content {
  min-width: 0;
  padding-right: 4px;
}

.msg-content__text {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-color-2, #5c6675);
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-content :deep(a) {
  color: var(--primary-color, #1677ff);
  text-decoration: none;
}

.msg-content :deep(a:hover) {
  text-decoration: underline;
}

.msg-content :deep(table) {
  width: 100%;
  margin: 8px 0 12px;
  border-collapse: collapse;
  font-size: 13px;
}

.msg-content :deep(th),
.msg-content :deep(td) {
  border: 1px solid var(--border-color, #e8edf4);
  padding: 8px 10px;
  text-align: left;
}

.msg-content :deep(th) {
  background: var(--body-color, #f5f7fb);
  font-weight: 600;
}

.msg-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
