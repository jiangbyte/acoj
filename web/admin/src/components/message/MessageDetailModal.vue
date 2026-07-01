<script setup lang="ts">
import { messageApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

type DetailType = 'notification' | 'message' | 'todo'

const emit = defineEmits<{
  changed: [payload: { type: DetailType; id: string }]
}>()

const { t } = useI18n()
const state = reactive({
  show: false,
  loading: false,
  actionLoading: false,
  type: 'notification' as DetailType,
  source: {} as any,
  detail: {} as any,
  messages: [] as any[],
  replyContent: '',
})

const title = computed(() => {
  if (state.type === 'notification') {
    return t('resource.message.notification.detail_notification')
  }
  if (state.type === 'message') {
    return t('resource.message.message.detail_message')
  }
  return t('resource.message.todo.detail_todo')
})

async function open(type: DetailType, source: any) {
  state.type = type
  state.source = source ?? {}
  state.detail = {}
  state.messages = []
  state.replyContent = ''
  state.show = true
  await fetchDetail()
  await acknowledgeOpen()
}

async function fetchDetail() {
  state.loading = true
  try {
    if (state.type === 'notification') {
      const response = await messageApi.myNotificationDetail({ id: state.source.id })
      state.detail = response.data ?? {}
      return
    }
    if (state.type === 'todo') {
      const response = await messageApi.myTodoDetail({ id: state.source.id })
      state.detail = response.data ?? {}
      return
    }
    state.detail = state.source
    const response = await messageApi.myThreadMessages({
      thread_id: state.source.id,
      current: 1,
      size: 20,
    })
    state.messages = response.data?.records ?? []
  } finally {
    state.loading = false
  }
}

async function markNotificationRead() {
  const id = state.detail.id || state.source.id
  if (!id) {
    return
  }
  state.actionLoading = true
  try {
    await messageApi.readNotifications({ ids: [id] })
    state.detail.is_read = true
    state.source.is_read = true
    emit('changed', { type: 'notification', id })
  } finally {
    state.actionLoading = false
  }
}

async function acknowledgeOpen() {
  const id = state.detail.id || state.source.id
  if (!id) {
    return
  }
  if (state.type === 'notification' && !(state.detail.is_read || state.source.is_read)) {
    await messageApi.readNotifications({ ids: [id] })
    state.detail.is_read = true
    state.source.is_read = true
    emit('changed', { type: 'notification', id })
  } else if (state.type === 'message' && (state.source.unread_count ?? 0) > 0) {
    await messageApi.readThread({ thread_id: id })
    state.source.unread_count = 0
    emit('changed', { type: 'message', id })
  } else if (state.type === 'todo' && !todoAssigneeStatus()) {
    await messageApi.startTodo({ todo_id: id })
    state.detail.assignee_status = 'IN_PROGRESS'
    state.source.assignee_status = 'IN_PROGRESS'
    emit('changed', { type: 'todo', id })
  }
}

async function replyMessage() {
  const content = state.replyContent.trim()
  if (!content) {
    window.$message.warning(t('resource.message.message.reply_required'))
    return
  }
  state.actionLoading = true
  try {
    await messageApi.replyMessage({
      thread_id: state.source.id,
      content,
    })
    state.replyContent = ''
    await fetchDetail()
    emit('changed', { type: 'message', id: state.source.id })
  } finally {
    state.actionLoading = false
  }
}

async function updateTodo(action: 'start' | 'complete' | 'cancel') {
  const id = state.detail.id || state.source.id
  if (!id) {
    return
  }
  state.actionLoading = true
  try {
    if (action === 'start') {
      await messageApi.startTodo({ todo_id: id })
    } else if (action === 'complete') {
      await messageApi.completeTodo({ todo_id: id })
    } else {
      await messageApi.cancelTodo({ todo_id: id })
    }
    await fetchDetail()
    emit('changed', { type: 'todo', id })
  } finally {
    state.actionLoading = false
  }
}

function todoAssigneeStatus() {
  return String(state.detail.assignee_status || state.source.assignee_status || '')
}

function canStartTodo() {
  const status = todoAssigneeStatus()
  return !status || status === 'PENDING'
}

function canCompleteTodo() {
  const status = todoAssigneeStatus()
  return status === 'PENDING' || status === 'IN_PROGRESS'
}

function canCancelTodo() {
  const status = todoAssigneeStatus()
  return status !== 'COMPLETED' && status !== 'CANCELLED'
}

defineExpose({ open })
</script>

<template>
  <NModal
    v-model:show="state.show"
    preset="card"
    draggable
    :mask-closable="false"
    :title="title"
    style="width: 720px"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <template v-if="state.type === 'notification'">
          <NDescriptions label-placement="left" bordered :column="1">
            <NDescriptionsItem :label="t('resource.message.notification.title_field')">
              {{ displayValue(state.detail.title || state.source.title) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.notification.severity')">
              <NTag
                :color="
                  createTagColor(dictTypeColor('NOTIFICATION_SEVERITY', state.detail.severity))
                "
                :bordered="false"
              >
                {{
                  dictTypeData('NOTIFICATION_SEVERITY', state.detail.severity) ||
                  displayValue(state.detail.severity)
                }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.status')">
              <NTag
                :type="state.detail.is_read || state.source.is_read ? 'success' : 'warning'"
                :bordered="false"
              >
                {{
                  state.detail.is_read || state.source.is_read
                    ? t('app.notice.read')
                    : t('app.notice.unread')
                }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.notification.publish_at')">
              {{ displayValue(state.detail.publish_at || state.source.publish_at) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.notification.content')">
              {{ displayValue(state.detail.content || state.source.content) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.created_at')">
              {{ displayValue(state.detail.created_at) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.updated_at')">
              {{ displayValue(state.detail.updated_at) }}
            </NDescriptionsItem>
          </NDescriptions>
          <NSpace class="mt-4" justify="end">
            <NButton
              v-if="!(state.detail.is_read || state.source.is_read)"
              type="primary"
              :loading="state.actionLoading"
              @click="markNotificationRead"
            >
              {{ t('resource.message.notification.mark_read') }}
            </NButton>
          </NSpace>
        </template>

        <template v-else-if="state.type === 'message'">
          <NDescriptions label-placement="left" bordered :column="1">
            <NDescriptionsItem :label="t('resource.message.message.thread_title')">
              {{ displayValue(state.detail.title || state.source.title) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.message.thread_type')">
              <NTag
                :color="
                  createTagColor(
                    dictTypeColor(
                      'MESSAGE_THREAD_TYPE',
                      state.detail.thread_type || state.source.thread_type,
                    ),
                  )
                "
                :bordered="false"
              >
                {{
                  dictTypeData(
                    'MESSAGE_THREAD_TYPE',
                    state.detail.thread_type || state.source.thread_type,
                  ) || displayValue(state.detail.thread_type || state.source.thread_type)
                }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.updated_at')">
              {{ displayValue(state.detail.updated_at || state.source.updated_at) }}
            </NDescriptionsItem>
          </NDescriptions>
          <NDivider />
          <NList v-if="state.messages.length" bordered>
            <NListItem v-for="item in state.messages" :key="item.id">
              <NThing>
                <template #header>
                  {{
                    item.sender_name ||
                    item.sender_account_id ||
                    t('resource.message.message.system_sender')
                  }}
                </template>
                <template #description>
                  {{ displayValue(item.created_at) }}
                </template>
                {{ displayValue(item.content) }}
              </NThing>
            </NListItem>
          </NList>
          <NEmpty v-else class="py-32px" :description="t('resource.message.message.no_messages')" />
          <NInput
            v-model:value="state.replyContent"
            class="mt-4"
            type="textarea"
            :placeholder="t('resource.message.message.reply_placeholder')"
          />
          <NSpace class="mt-3" justify="end">
            <NButton type="primary" :loading="state.actionLoading" @click="replyMessage">
              {{ t('resource.message.message.reply') }}
            </NButton>
          </NSpace>
        </template>

        <template v-else>
          <NDescriptions label-placement="left" bordered :column="1">
            <NDescriptionsItem :label="t('resource.message.todo.title_field')">
              {{ displayValue(state.detail.title || state.source.title) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.todo.priority')">
              <NTag
                :color="createTagColor(dictTypeColor('TODO_PRIORITY', state.detail.priority))"
                :bordered="false"
              >
                {{
                  dictTypeData('TODO_PRIORITY', state.detail.priority) ||
                  displayValue(state.detail.priority)
                }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.status')">
              <NTag
                :color="createTagColor(dictTypeColor('TODO_STATUS', state.detail.status))"
                :bordered="false"
              >
                {{
                  dictTypeData('TODO_STATUS', state.detail.status) ||
                  displayValue(state.detail.status)
                }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.todo.assignee_status')">
              <NTag
                :color="createTagColor(dictTypeColor('TODO_STATUS', todoAssigneeStatus()))"
                :bordered="false"
              >
                {{
                  dictTypeData('TODO_STATUS', todoAssigneeStatus()) ||
                  displayValue(todoAssigneeStatus())
                }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.todo.due_at')">
              {{ displayValue(state.detail.due_at || state.source.due_at) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('resource.message.todo.content')">
              {{ displayValue(state.detail.content || state.source.content) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.created_at')">
              {{ displayValue(state.detail.created_at) }}
            </NDescriptionsItem>
            <NDescriptionsItem :label="t('common.often.updated_at')">
              {{ displayValue(state.detail.updated_at) }}
            </NDescriptionsItem>
          </NDescriptions>
          <NSpace class="mt-4" justify="end">
            <NButton
              v-if="canStartTodo()"
              type="info"
              :loading="state.actionLoading"
              @click="updateTodo('start')"
            >
              {{ t('resource.message.todo.start_task') }}
            </NButton>
            <NButton
              v-if="canCompleteTodo()"
              type="success"
              :loading="state.actionLoading"
              @click="updateTodo('complete')"
            >
              {{ t('resource.message.todo.complete_task') }}
            </NButton>
            <NButton
              v-if="canCancelTodo()"
              type="warning"
              :loading="state.actionLoading"
              @click="updateTodo('cancel')"
            >
              {{ t('resource.message.todo.cancel_task') }}
            </NButton>
          </NSpace>
        </template>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
